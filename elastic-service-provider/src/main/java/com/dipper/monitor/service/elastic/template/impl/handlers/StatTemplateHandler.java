package com.dipper.monitor.service.elastic.template.impl.handlers;

import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.entity.elastic.life.EsTemplateConfigMes;
import com.dipper.monitor.entity.elastic.segments.SegmentMessage;
import com.dipper.monitor.entity.elastic.shard.ShardEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.life.ElasticRealLifecyclePoliciesService;
import com.dipper.monitor.service.elastic.segment.ElasticSegmentService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

@Slf4j
public class StatTemplateHandler {

    private ElasticClientService elasticClientService;
    private ElasticRealIndexService elasticRealIndexService;
    private ElasticShardService elasticShardService;
    private ElasticSegmentService elasticSegmentService;
    private ElasticRealLifecyclePoliciesService elasticRealLifecyclePoliciesService;

    public StatTemplateHandler(ElasticClientService elasticClientService,
                               ElasticRealIndexService elasticRealIndexService,
                               ElasticShardService elasticShardService,
                               ElasticSegmentService elasticSegmentService,
                               ElasticRealLifecyclePoliciesService elasticRealLifecyclePoliciesService) {
        this.elasticClientService = elasticClientService;
        this.elasticRealIndexService = elasticRealIndexService;
        this.elasticShardService = elasticShardService;
        this.elasticSegmentService = elasticSegmentService;
        this.elasticRealLifecyclePoliciesService = elasticRealLifecyclePoliciesService;
    }

    public List<EsTemplateConfigMes> statTemplate(String name) throws IOException {
        long startTime = System.currentTimeMillis();

        Map<String, EsLifeCycleManagement> lifeErrors = getLifeCycleBadMap();
        log.info("生命周期检查耗时: {} ms", System.currentTimeMillis() - startTime);

        Map<String, IndexEntity> indexMap = elasticRealIndexService.listIndexMap(false);
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
        List<EsLifeCycleManagement> result = elasticRealLifecyclePoliciesService.getLifeCycleList();
        if (result.isEmpty()) return Collections.emptyMap();

        Map<String, EsLifeCycleManagement> map = new HashMap<>();
        for (EsLifeCycleManagement item : result) map.put(item.getIndex(), item);
        return map;
    }



    // 假设的辅助方法，用于根据模板名称对索引进行分组
    private Map<EsTemplateConfigMes, List<IndexEntity>> groupIndexByTem(String templateName, Map<String, IndexEntity> indexMap) {
        // 实现逻辑根据实际情况调整
        return new HashMap<>();
    }
}