package com.dipper.monitor.service.elastic.policy.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.policy.PolicyPageRequest;
import com.dipper.monitor.entity.elastic.policy.response.LifePolicyResponse;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.policy.LifePolicyRealService;
import com.dipper.monitor.service.elastic.policy.LifePolicyStoreService;
import com.dipper.monitor.utils.ListUtils;
import com.dipper.monitor.utils.Tuple2;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class LifePolicyRealServiceImpl implements LifePolicyRealService {

    @Autowired
    private LifePolicyStoreService lifePolicyStoreService;
    @Autowired
    private ElasticClientService elasticClientService;

    @PostConstruct
    public void init() {
        try {
            policyAllRefresh();
        }catch (Exception e){
            log.error("init error", e);
        }
    }


    @Override
    public void policyAllRefresh() {
        List<LifePolicyResponse> allPolicies = lifePolicyStoreService.getAllPolicies();
        if(allPolicies == null || allPolicies.isEmpty()){
            return;
        }
        for (LifePolicyResponse lifePolicyResponse : allPolicies) {
            Integer id = lifePolicyResponse.getId();
            try {
                policyEffective(id);
            } catch (IOException e) {
                log.error("策略生效失败",e);
            }
        }
    }

    /**
     * 让某个策略实时生效
     * @param id
     */
    @Override
    public void policyEffective(Integer id) throws IOException {
        LifePolicyResponse onePolicy = lifePolicyStoreService.getOnePolicy(id);
        if (onePolicy == null) {
            throw new IllegalArgumentException("策略不存在");
        }
        String enName = onePolicy.getEnName();
        String policyContent = onePolicy.getPolicyContent();

        String api = "/_ilm/policy/"+enName;
        NStringEntity nStringEntity = new NStringEntity(policyContent);
        Response response = elasticClientService.executePutApiReturnResponse(api, nStringEntity);

        if(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201){
            log.info("策略生效成功");
        }else {
            String responseData = EntityUtils.toString(response.getEntity());
            log.error("策略生效失败,{}", responseData);
        }
    }

    @Override
    public Map<String,String>  policyList() throws UnsupportedEncodingException, IOException {
        // 获取所有策略 根据这个接口 GET /_ilm/policy
        String api = "/_ilm/policy";
        String response = elasticClientService.executeGetApi(api);
        JSONObject jsonObject = JSONObject.parseObject(response);
        Map<String,String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            result.put(entry.getKey(),entry.getValue().toString());
        }
        return result;
    }

    @Override
    public Tuple2<List<LifePolicyResponse>, Long> getRealPolicies(PolicyPageRequest request) {
        try {
            // 1. 获取所有策略 Map（key = policyName, value = policyJson）
            Map<String, String> allPolicies = policyList();

            // 2. 转换为 List<LifePolicyResponse>
            List<LifePolicyResponse> policyResponses = new ArrayList<>();
            int id = 1;
            for (Map.Entry<String, String> entry : allPolicies.entrySet()) {
                LifePolicyResponse response = new LifePolicyResponse();
                response.setId(id++);
                response.setZhName(entry.getKey()); // 可以理解为中文名或策略名
                response.setEnName(entry.getKey()); // 如果没有英文名，先复用 key
                response.setPolicyContent(entry.getValue()); // JSON 字符串
                response.setEffectStatus("生效"); // 假设默认都生效
                response.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                policyResponses.add(response);
            }

            // 3. 获取总数
            long total = policyResponses.size();

            // 4. 分页处理
            int pageNum = Math.max(request.getPageNum(), 1);
            int pageSize = Math.max(request.getPageSize(), 10);

            List<List<LifePolicyResponse>> pagedList = ListUtils.splitListBySize(policyResponses, pageSize);

            // 5. 获取当前页数据（注意是否超出范围）
            List<LifePolicyResponse> currentPageData = Collections.emptyList();
            if (pageNum <= pagedList.size()) {
                currentPageData = pagedList.get(pageNum - 1);
            }

            // 6. 返回 Tuple2（当前页数据，总数量）
            return new Tuple2<>(currentPageData, total);

        } catch (Exception e) {
            // 异常处理（如日志记录）
            return new Tuple2<>(Collections.emptyList(), 0L);
        }
    }

    @Override
    public boolean deletePolicy(String policyName) {
        if (StringUtils.isBlank(policyName)) {
            return false;
        }

        String api = "/_ilm/policy/" + policyName;

        try {
            // 调用 DELETE 请求删除策略
            String response = elasticClientService.executeDeleteApi(api,null);

            // 假设成功删除返回 {"acknowledged":true}
            JSONObject jsonObject = JSON.parseObject(response);
            return jsonObject != null && jsonObject.getBooleanValue("acknowledged");
        } catch (Exception e) {
           log.error("删除策略失败", e);
            return false;
        }
    }
}
