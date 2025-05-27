package com.dipper.monitor.service.elastic.slowsearch.handlers.slow;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.slowsearch.slow.QueryOptimizationReq;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.query.AggOptimizationHandler;
import com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.query.GenerallOptimizationHandler;
import com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.query.ScrollOptimizationHandler;
import com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.query.SearchOptimizationHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OneQueryOptimizationHandler {

    private final ElasticClientService elasticClientService;
    private final ElasticRealIndexService elasticRealIndexService;

    public OneQueryOptimizationHandler(ElasticClientService elasticClientService) {
        this.elasticClientService = elasticClientService;
        this.elasticRealIndexService = SpringUtil.getBean(ElasticRealIndexService.class);
    }

    public String queryOptimization(QueryOptimizationReq queryOptimizationReq) {
        if (queryOptimizationReq == null || queryOptimizationReq.getQueryId() == null) {
            log.error("查询优化请求参数不完整");
            return createErrorResponse("查询优化请求参数不完整");
        }

        try {
            // 获取索引 setting 和 mapping 信息
            String indexName = queryOptimizationReq.getIndexName();
            JSONObject setting = elasticRealIndexService.getIndexSetting(indexName);
            JSONObject mappings = elasticRealIndexService.getMappingByIndexName(indexName);

            // 构建优化结果对象
            JSONObject optimizationResult = new JSONObject();

            // 设置原始查询执行时间（模拟值）
            optimizationResult.put("originalTime", 6500); // 假设原查询耗时6500ms

            // 根据查询类型生成优化建议
            switch (queryOptimizationReq.getQueryType()) {
                case "search":
                    SearchOptimizationHandler searchOptimizationHandler = new SearchOptimizationHandler(setting, mappings);
                    searchOptimizationHandler.generateSearchQueryOptimization(optimizationResult, mappings);
                    break;
                case "aggregation":
                    AggOptimizationHandler aggOptimizationHandler = new AggOptimizationHandler(setting, mappings);
                    aggOptimizationHandler.generateAggregationQueryOptimization(optimizationResult, mappings);
                    break;
                case "scroll":
                    ScrollOptimizationHandler scrollOptimizationHandler = new ScrollOptimizationHandler(setting, mappings);
                    scrollOptimizationHandler.generateScrollQueryOptimization(optimizationResult, mappings);
                    break;
                default:
                    GenerallOptimizationHandler generallOptimizationHandler = new GenerallOptimizationHandler(setting, mappings);
                    generallOptimizationHandler.generateDefaultOptimization(optimizationResult, mappings);
            }

            return optimizationResult.toJSONString();

        } catch (Exception e) {
            log.error("生成查询优化建议失败", e);
            return createErrorResponse("生成查询优化建议失败: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorMessage) {
        JSONObject error = new JSONObject();
        error.put("error", errorMessage);
        return error.toJSONString();
    }
}