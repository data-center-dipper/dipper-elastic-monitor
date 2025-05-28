package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index.query;


import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.slowsearch.slow.index.SlowQueryAnalysisResult;

import java.util.HashMap;
import java.util.Map;

public class ScriptQueryOptimizer {

    public SlowQueryAnalysisResult optimize(SlowQueryAnalysisResult result,
                                            Map<String, Object> originalQuery) {
        result.setIssueType("脚本查询");
        result.setImpactLevel("high");
        result.setExplanation("脚本查询执行效率较低，会消耗大量CPU资源。建议使用预计算字段或更高效的查询方式替代。");
        result.setOptimizedExecutionTime(result.getOriginalExecutionTime() / 2);
        Map<String, Object> stringObjectMap = optimizeScriptQuery(originalQuery);
        JSONObject jsonObject = new JSONObject(stringObjectMap);
        result.setOptimizedQuery(jsonObject.toJSONString());
        return result;
    }

    private Map<String, Object> optimizeScriptQuery(Map<String, Object> original) {
        Map<String, Object> optimized = new HashMap<>(original);
        // 建议替换为预计算字段或使用更高效的查询方式
        // 这里只是示例，实际优化需要根据具体script内容

        // 添加缓存参数
        if (optimized.containsKey("query")) {
            Map<String, Object> query = (Map<String, Object>) optimized.get("query");
            if (query.containsKey("script")) {
                Map<String, Object> script = (Map<String, Object>) query.get("script");
                script.put("cache", true);
            }
        }

        return optimized;
    }
}