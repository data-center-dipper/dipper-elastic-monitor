package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index.query;


import com.dipper.monitor.entity.elastic.slowsearch.slow.index.SlowQueryAnalysisResult;
import com.dipper.monitor.utils.JsonUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WildcardQueryOptimizer {

    public SlowQueryAnalysisResult optimize(SlowQueryAnalysisResult result,
                                            Map<String, Object> originalQuery) {
        result.setIssueType("通配符前缀查询");
        result.setImpactLevel("high");
        result.setExplanation("通配符前缀查询无法使用倒排索引，会导致全表扫描，严重影响性能。" +
                "建议改用 match_phrase 或 prefix 查询。");
        result.setOptimizedExecutionTime(1000L);
        result.setOptimizedQuery(JsonUtils.toJson(optimizeWildcardQuery(originalQuery)));
        return result;
    }

    private Map<String, Object> optimizeWildcardQuery(Map<String, Object> original) {
        Map<String, Object> optimized = new HashMap<>(original);
        Map<String, Object> query = (Map<String, Object>) optimized.get("query");
        Map<String, Object> wildcard = (Map<String, Object>) query.get("wildcard");

        Map<String, Object> matchPhrase = new HashMap<>();
        for (Map.Entry<String, Object> entry : wildcard.entrySet()) {
            String field = entry.getKey();
            String value = (String) entry.getValue();
            matchPhrase.put(field, value.replaceAll("\\*+", ""));
        }

        optimized.put("query", Collections.singletonMap("match_phrase", matchPhrase));
        optimized.put("size", 1000);
        return optimized;
    }
}