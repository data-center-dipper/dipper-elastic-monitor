package com.dipper.monitor.service.elastic.template.impl.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.entity.elastic.life.EsTemplateConfigMes;
import com.dipper.monitor.entity.elastic.segments.SegmentMessage;
import com.dipper.monitor.entity.elastic.shard.ShardEntity;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticIndexService;
import com.dipper.monitor.service.elastic.segment.ElasticSegmentService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import org.springframework.beans.BeanUtils;

@Slf4j
public class StatTemplateHandler {

    private ElasticClientService elasticClientService;
    private ElasticIndexService elasticIndexService;
    private ElasticShardService elasticShardService;
    private ElasticSegmentService elasticSegmentService;

    public StatTemplateHandler(ElasticClientService elasticClientService,
                               ElasticIndexService elasticIndexService,
                               ElasticShardService elasticShardService,
                               ElasticSegmentService elasticSegmentService) {
        this.elasticClientService = elasticClientService;
        this.elasticIndexService = elasticIndexService;
        this.elasticShardService = elasticShardService;
        this.elasticSegmentService = elasticSegmentService;
    }

    public List<EsTemplateConfigMes> statTemplate(String name) throws IOException {
        long startTime = System.currentTimeMillis();

        Map<String, EsLifeCycleManagement> lifeErrors = getLifeCycleBadMap();
        log.info("生命周期检查耗时: {} ms", System.currentTimeMillis() - startTime);

        Map<String, IndexEntity> indexMap = elasticIndexService.listIndexMap(false);
        log.info("索引列表获取耗时: {} ms", System.currentTimeMillis());

        Map<String, List<ShardEntity>> listShard = elasticShardService.listShardMap();
        log.info("分片信息获取耗时: {} ms", System.currentTimeMillis());

        Map<String, List<SegmentMessage>> segmentMap = elasticSegmentService.segmentMap();
        log.info("段信息获取耗时: {} ms", System.currentTimeMillis());

        Map<EsTemplateConfigMes, List<IndexEntity>> groupIndex = groupIndexByTem(name, indexMap);

        // todo:未完待续
        return null;
    }

    private Map<String, EsLifeCycleManagement> getLifeCycleBadMap() {
        List<EsLifeCycleManagement> result = getLifeCycleList();
        if (result.isEmpty()) return Collections.emptyMap();

        Map<String, EsLifeCycleManagement> map = new HashMap<>();
        for (EsLifeCycleManagement item : result) map.put(item.getIndex(), item);
        return map;
    }

    private List<EsLifeCycleManagement> getLifeCycleList() {
        try {
            String result = elasticClientService.executeGetApi(ElasticRestApi.LIFE_CYCLE_MANAGEMENT.getApiPath());
            if (StringUtils.isBlank(result) || result.contains("master_not_discovered_exception")) return Collections.emptyList();

            JSONObject jsonObject = JSON.parseObject(result).getJSONObject("indices");
            List<EsLifeCycleManagement> list = new ArrayList<>();
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                JSONObject value = (JSONObject) entry.getValue();
                if (!"false".equals(value.getString("managed")) && "ERROR".equalsIgnoreCase(value.getString("step"))) {
                    EsLifeCycleManagement management = new EsLifeCycleManagement();
                    management.setIndex(entry.getKey());
                    management.setMessage(value.toJSONString());
                    list.add(management);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("获取生命周期管理列表异常: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // 假设的辅助方法，用于根据模板名称对索引进行分组
    private Map<EsTemplateConfigMes, List<IndexEntity>> groupIndexByTem(String templateName, Map<String, IndexEntity> indexMap) {
        // 实现逻辑根据实际情况调整
        return new HashMap<>();
    }
}