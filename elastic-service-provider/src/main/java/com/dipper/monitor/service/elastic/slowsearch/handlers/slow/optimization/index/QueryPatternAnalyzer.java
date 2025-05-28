package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index;

import com.dipper.monitor.entity.elastic.slowsearch.slow.index.SlowQueryAnalysisResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class QueryPatternAnalyzer {

    public Map<String, Integer> analyzePatterns(List<SlowQueryAnalysisResult> analysisResults) {
        return analysisResults.stream()
                .collect(Collectors.groupingBy(
                        SlowQueryAnalysisResult::getIssueType,
                        Collectors.summingInt(result -> 1)
                ));
    }
    
    /**
     * 分析查询模式，识别常见的查询模式
     */
    public Map<String, Integer> analyzeQueryPatterns(List<SlowQueryAnalysisResult> analysisResults) {
        Map<String, Integer> patternCounts = new HashMap<>();
        
        for (SlowQueryAnalysisResult result : analysisResults) {
            String originalQuery = result.getOriginalQuery();
            String pattern = identifyQueryPattern(originalQuery);
            
            if (pattern != null) {
                patternCounts.put(pattern, patternCounts.getOrDefault(pattern, 0) + 1);
            }
        }
        
        return patternCounts;
    }
    
    /**
     * 识别查询模式
     */
    private String identifyQueryPattern(String queryJson) {
        // 这里可以实现更复杂的查询模式识别逻辑
        // 例如：识别常见的查询模板、查询结构等
        if (queryJson.contains("match_all")) {
            return "全表扫描查询";
        } else if (queryJson.contains("wildcard") && queryJson.contains("*")) {
            return "通配符查询";
        } else if (queryJson.contains("script")) {
            return "脚本查询";
        } else if (queryJson.contains("aggs") || queryJson.contains("aggregations")) {
            return "聚合查询";
        } else if (queryJson.contains("size") && (queryJson.contains("\"size\":1000") || queryJson.contains("\"size\":5000"))) {
            return "大结果集查询";
        } else if (queryJson.contains("bool") && (queryJson.contains("must") || queryJson.contains("should"))) {
            return "布尔查询";
        } else if (queryJson.contains("range")) {
            return "范围查询";
        } else if (queryJson.contains("match_phrase")) {
            return "短语匹配查询";
        } else if (queryJson.contains("term") || queryJson.contains("terms")) {
            return "精确匹配查询";
        } else if (queryJson.contains("regexp")) {
            return "正则表达式查询";
        } else if (queryJson.contains("fuzzy")) {
            return "模糊查询";
        } else if (queryJson.contains("prefix")) {
            return "前缀查询";
        } else if (queryJson.contains("exists")) {
            return "字段存在查询";
        } else if (queryJson.contains("nested")) {
            return "嵌套查询";
        } else {
            return "其他查询类型";
        }
    }
}