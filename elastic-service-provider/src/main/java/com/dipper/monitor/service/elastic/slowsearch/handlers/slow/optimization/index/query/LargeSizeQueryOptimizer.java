package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index.query;


import com.dipper.monitor.entity.elastic.slowsearch.slow.index.SlowQueryAnalysisResult;
import com.dipper.monitor.utils.JsonUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LargeSizeQueryOptimizer {

    public SlowQueryAnalysisResult optimize(SlowQueryAnalysisResult result,
                                            Map<String, Object> originalQuery) {
        result.setIssueType("大结果集查询");
        result.setImpactLevel("medium");
        result.setExplanation("返回大量结果会消耗内存和网络资源，建议减少 size 参数或使用 scroll API 分页查询。");
        result.setOptimizedExecutionTime(800L);
        result.setOptimizedQuery(JsonUtils.toJson(optimizeLargeSizeQuery(originalQuery)));
        return result;
    }

    private Map<String, Object> optimizeLargeSizeQuery(Map<String, Object> original) {
        Map<String, Object> optimized = new HashMap<>(original);
        optimized.put("size", 100);
        optimized.put("sort", Collections.singletonList(Collections.singletonMap("_id", "asc")));
        return optimized;
    }
}