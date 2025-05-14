package com.dipper.monitor.service.elastic.template.impl.handlers;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.cluster.ClusterHealth;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.enums.elastic.RollingIndexEnum;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.service.elastic.template.TemplatePreviewService;
import com.dipper.monitor.service.elastic.template.impl.handlers.rolling.*;
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
        ClusterHealth healthData = elasticHealthService.getHealthData();
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
                NotRollingIndexHandler notRollingIndexHandler = new NotRollingIndexHandler(esUnconvertedTemplate,
                        elasticStoreTemplateService,templatePreviewService);
                notRollingIndexHandler.handle();
            case DAILY:
                DailyRollingIndexHandler dailyRollingIndexHandler = new DailyRollingIndexHandler(esUnconvertedTemplate);
                dailyRollingIndexHandler.handle();
                break;
            case EVERY_7_DAYS:
                Every2DaysRollingIndexHandler every2DaysRollingIndexHandler = new Every2DaysRollingIndexHandler(esUnconvertedTemplate);
                every2DaysRollingIndexHandler.handle();
                break;
            case EVERY_10_DAYS:
                Every10DaysRollingIndexHandler every10DaysRollingIndexHandler = new Every10DaysRollingIndexHandler(esUnconvertedTemplate);
                every10DaysRollingIndexHandler.handle();
                break;
            case EVERY_15_DAYS:
                Every15DaysRollingIndexHandler every15DaysRollingIndexHandler = new Every15DaysRollingIndexHandler(esUnconvertedTemplate);
                every15DaysRollingIndexHandler.handle();
                break;
            case EVERY_30_DAYS:
                Every30DaysRollingIndexHandler every30DaysRollingIndexHandler = new Every30DaysRollingIndexHandler(esUnconvertedTemplate);
                every30DaysRollingIndexHandler.handle();
                break;
            case EVERY_60_DAYS:
                Every60DaysRollingIndexHandler every60DaysRollingIndexHandler = new Every60DaysRollingIndexHandler(esUnconvertedTemplate);
                every60DaysRollingIndexHandler.handle();
                break;
            case EVERY_90_DAYS:
                Every90DaysRollingIndexHandler every90DaysRollingIndexHandler = new Every90DaysRollingIndexHandler(esUnconvertedTemplate);
                every90DaysRollingIndexHandler.handle();
                break;
            case EVERY_180_DAYS:
                Every180DaysRollingIndexHandler every180DaysRollingIndexHandler = new Every180DaysRollingIndexHandler(esUnconvertedTemplate);
                every180DaysRollingIndexHandler.handle();
                break;
            case EVERY_365_DAYS:
                Every360DaysRollingIndexHandler every360DaysRollingIndexHandler = new Every360DaysRollingIndexHandler(esUnconvertedTemplate);
                every360DaysRollingIndexHandler.handle();
                break;
            default:
                throw new IllegalArgumentException("不支持的滚动索引类型");
        }

    }
}
