package com.dipper.monitor.service.elastic.policy.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.policy.response.LifePolicyResponse;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.policy.LifePolicyRealService;
import com.dipper.monitor.service.elastic.policy.LifePolicyStoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LifePolicyRealServiceImpl implements LifePolicyRealService {

    @Autowired
    private LifePolicyStoreService lifePolicyStoreService;
    @Autowired
    private ElasticClientService elasticClientService;

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
}
