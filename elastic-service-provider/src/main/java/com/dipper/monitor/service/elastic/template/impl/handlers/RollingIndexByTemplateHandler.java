package com.dipper.monitor.service.elastic.template.impl.handlers;

import com.dipper.monitor.entity.elastic.cluster.ClusterHealth;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.service.elastic.template.impl.handlers.rolling.NotRollingIndexHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.rolling.OneDayRollingIndexHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RollingIndexByTemplateHandler {


    private ElasticHealthService elasticHealthService;

    public RollingIndexByTemplateHandler(ElasticHealthService elasticHealthService) {
        this.elasticHealthService = elasticHealthService;
    }

    public void rollIndexByTemplate(EsUnconvertedTemplate esUnconvertedTemplate) {
        ClusterHealth healthData = elasticHealthService.getHealthData();
        if (healthData == null) {
            throw new RuntimeException("集群健康状态异常");
        }
        String status = healthData.getStatus();
        if (!"green".equals(status)) {
            throw new RuntimeException("集群状态异常");
        }
        String activeShardsPercent = healthData.getActiveShardsPercent();
        double activeShardsNum = Double.parseDouble(activeShardsPercent.replace("%", ""));
        if(activeShardsNum < 80){
            throw new RuntimeException("集群未分配的分片小于80%不能进行索引滚动");
        }
        Integer numberOfShards = esUnconvertedTemplate.getNumberOfShards();
        Integer numberOfReplicas = esUnconvertedTemplate.getNumberOfReplicas();
        if (numberOfShards == null || numberOfReplicas == null) {
            throw new RuntimeException("索引模板的shards和replicas不能为空");
        }
        if (numberOfShards < 1 || 50 < numberOfReplicas) {
            throw new RuntimeException("索引模板的shards和replicas必须在1-50之间");
        }
        log.info("集群健康状态正常，开始删除未来，生成新的模版，滚动创建未来索引");
        deleteFeatureAndRollToDayCreateFutureIndex(esUnconvertedTemplate);
    }

    private void deleteFeatureAndRollToDayCreateFutureIndex(EsUnconvertedTemplate esUnconvertedTemplate) {
        Integer rollingPeriod = esUnconvertedTemplate.getRollingPeriod();
        String indexPatterns = esUnconvertedTemplate.getIndexPatterns();

        if(rollingPeriod == null || indexPatterns == null){
            throw new RuntimeException("滚动周期和索引模板不能为空");
        }
        if(rollingPeriod < 1 ){
            NotRollingIndexHandler notRollingIndexHandler = new NotRollingIndexHandler(esUnconvertedTemplate);
            notRollingIndexHandler.handle();
            return;
        }
        if (rollingPeriod == 1) {
            OneDayRollingIndexHandler oneDayRollingIndexHandler = new OneDayRollingIndexHandler(esUnconvertedTemplate);
            oneDayRollingIndexHandler.handle();
        }
    }
}
