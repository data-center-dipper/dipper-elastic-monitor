package com.dipper.monitor.service.elastic.life.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.life.LifecyclePoliciesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class LifecyclePoliciesServiceImpl implements LifecyclePoliciesService {

    @Autowired
    private ElasticClientService elasticClientService;

    public List<JSONObject> getLifeCycleList() {
        try {
            // 创建请求并设置空请求体
            String ilmExplainResult = elasticClientService.executeGetApi("/*/_ilm/explain?pretty");
            log.info("索引生命周期管理状态：{}", ilmExplainResult);

           List<JSONObject> result = new ArrayList<>();
            JSONObject jsonObject = JSONObject.parseObject(ilmExplainResult);
            JSONObject indices = jsonObject.getJSONObject("indices");
            for (String index : indices.keySet()) {
                JSONObject indexInfo = indices.getJSONObject(index);
                String step = indexInfo.getString("c");
                Boolean managed = indexInfo.getBoolean("managed");
              if ("false".equals(managed)) {
                    continue;
                   }
               if (!"ERROR".equalsIgnoreCase(step)) {
                   continue;
                }
                result.add(indexInfo);
            }
            return result;
        } catch (Exception e) {
            log.error("检查ILM问题时发生错误", e);
        }
        return Collections.emptyList();
    }
}
