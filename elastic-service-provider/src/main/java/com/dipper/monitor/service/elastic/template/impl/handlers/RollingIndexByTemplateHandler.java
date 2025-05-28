package com.dipper.monitor.service.elastic.template.impl.handlers;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.enums.elastic.RollingIndexEnum;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.service.elastic.template.TemplatePreviewService;
import com.dipper.monitor.service.elastic.template.impl.handlers.rolling.immediately.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RollingIndexByTemplateHandler {


    private ElasticHealthService elasticHealthService;
    private ElasticStoreTemplateService elasticStoreTemplateService;
    private TemplatePreviewService templatePreviewService;
    private ElasticRealTemplateService elasticRealTemplateService;

    public RollingIndexByTemplateHandler(ElasticHealthService elasticHealthService,
                                         ElasticStoreTemplateService elasticStoreTemplateService,
                                         TemplatePreviewService templatePreviewService) {
        this.elasticHealthService = elasticHealthService;
        this.elasticStoreTemplateService = elasticStoreTemplateService;
        this.templatePreviewService = templatePreviewService;
        this.elasticRealTemplateService = SpringUtil.getBean(ElasticRealTemplateService.class);
    }

    public void rollIndexByTemplate(EsUnconvertedTemplate esUnconvertedTemplate) throws Exception {
        com.dipper.monitor.entity.elastic.cluster.ClusterHealth healthData = elasticHealthService.getHealthData();
        if (healthData == null) {
            throw new IllegalArgumentException("集群健康状态异常");
        }
        String status = healthData.getStatus();
        if ("green".equals(status) || "yellow".equals(status)) {

        }else {
            throw new IllegalArgumentException("集群状态异常");
        }
        String activeShardsPercent = healthData.getActiveShardsPercent();
        double activeShardsNum = Double.parseDouble(activeShardsPercent.replace("%", ""));
        if(activeShardsNum < 80){
            throw new IllegalArgumentException("集群未分配的分片小于80%不能进行索引滚动");
        }
        Integer numberOfShards = esUnconvertedTemplate.getNumberOfShards();
        Integer numberOfReplicas = esUnconvertedTemplate.getNumberOfReplicas();
        if (numberOfShards == null || numberOfReplicas == null) {
            throw new IllegalArgumentException("索引模板的shards和replicas不能为空");
        }
        if (numberOfShards < 1 || 50 < numberOfReplicas) {
            throw new IllegalArgumentException("索引模板的shards和replicas必须在1-50之间");
        }
        log.info("集群健康状态正常，开始删除未来，生成新的模版，滚动创建未来索引");
        deleteFeatureAndRollToDayCreateFutureIndex(esUnconvertedTemplate);
    }

    private void deleteFeatureAndRollToDayCreateFutureIndex(EsUnconvertedTemplate esUnconvertedTemplate) throws Exception {
        Integer rollingPeriod = esUnconvertedTemplate.getRollingPeriod();
        String indexPatterns = esUnconvertedTemplate.getIndexPatterns();

        if(rollingPeriod == null || indexPatterns == null){
            throw new RuntimeException("滚动周期和索引模板不能为空");
        }
        RollingIndexEnum rollingIndexEnum = RollingIndexEnum.fromDays(rollingPeriod);
        switch (rollingIndexEnum) {
            case NONE:
                NotRollingIndexHandler notRollingIndexHandler = new NotRollingIndexHandler(esUnconvertedTemplate);
                notRollingIndexHandler.handle();
            case DAILY:
                DaysOfRollingIndexHandler dailyRollingIndexHandler = new DaysOfRollingIndexHandler(esUnconvertedTemplate);
                dailyRollingIndexHandler.handle();
                break;
            case EVERY_30_DAYS:
                MonthRollingIndexHandler monthRollingIndexHandler = new MonthRollingIndexHandler(esUnconvertedTemplate);
                monthRollingIndexHandler.handle();
                break;
            case EVERY_180_DAYS:
                HafYearRollingIndexHandler hafYearRollingIndexHandler = new HafYearRollingIndexHandler(esUnconvertedTemplate);
                hafYearRollingIndexHandler.handle();
                break;
            case EVERY_365_DAYS:
                YearRollingIndexHandler yearRollingIndexHandler = new YearRollingIndexHandler(esUnconvertedTemplate);
                yearRollingIndexHandler.handle();
                break;
            default:
                throw new IllegalArgumentException("不支持的滚动索引类型");
        }

    }
}
