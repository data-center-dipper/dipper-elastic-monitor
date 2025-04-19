package com.dipper.monitor.service.elastic.life.impl;

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
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LifecyclePoliciesServiceImpl implements LifecyclePoliciesService {

    @Autowired
    private ElasticClientService elasticClientService;

    public Map<String, Object> getLifeCycleList() {
        try {
            // 创建请求并设置空请求体
            String ilmExplainResult = elasticClientService.executeGetApi("/_ilm/explain?pretty");
            log.info("索引生命周期管理状态：{}", ilmExplainResult);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> explainMap = objectMapper.readValue(ilmExplainResult, Map.class);

            return explainMap;
        } catch (Exception e) {
            log.error("检查ILM问题时发生错误", e);
        }
        return Collections.emptyMap();
    }
}
