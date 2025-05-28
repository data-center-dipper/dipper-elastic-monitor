package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index.query;


import com.dipper.monitor.entity.elastic.slowsearch.slow.index.SlowQueryAnalysisResult;
import com.dipper.monitor.utils.JsonUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AggregationOptimizer {

    public SlowQueryAnalysisResult optimize(SlowQueryAnalysisResult result,
                                            Map<String, Object> originalQuery) {
        result.setIssueType("复杂聚合查询");
        result.setImpactLevel("high");
        result.setExplanation("复杂聚合会显著增加 CPU 和内存开销，建议限制桶数量或在聚合前加 filter 过滤器。");
        result.setOptimizedExecutionTime(2000L);
        result.setOptimizedQuery(JsonUtils.toJson(optimizeAggregationQuery(originalQuery)));
        return result;
    }

    private Map<String, Object> optimizeAggregationQuery(Map<String, Object> original) {
        Map<String, Object> optimized = new HashMap<>(original);
        Map<String, Object> aggs = (Map<String, Object>) optimized.getOrDefault("aggs", optimized.get("aggregations"));

        if (aggs != null) {
            for (Object agg : aggs.values()) {
                if (agg instanceof Map) {
                    Map<String, Object> terms = (Map<String, Object>) ((Map<String, Object>) agg).get("terms");
                    if (terms != null) {
                        terms.put("size", 100); // 限制桶数量
                    }
                }
            }

            Map<String, Object> filterAgg = new HashMap<>();
            filterAgg.put("filter", Collections.singletonMap("range", Collections.singletonMap("timestamp", Collections.singletonMap("gte", "now-1d"))));
            filterAgg.put("aggs", aggs);

            optimized.put("aggs", Collections.singletonMap("filtered_aggs", filterAgg));
            optimized.remove("size"); // 不需要返回文档
        }

        return optimized;
    }
}