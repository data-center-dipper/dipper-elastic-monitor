package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.slowsearch.slow.index.SlowQueryAnalysisResult;
import com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index.query.AggregationOptimizer;
import com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index.query.LargeSizeQueryOptimizer;
import com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index.query.ScriptQueryOptimizer;
import com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index.query.WildcardQueryOptimizer;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.*;


public class QueryAnalysisService {

    private final AggregationOptimizer aggregationOptimizer;
    private final ScriptQueryOptimizer scriptQueryOptimizer;
    private final WildcardQueryOptimizer wildcardQueryOptimizer;
    private final LargeSizeQueryOptimizer largeSizeQueryOptimizer;

    public QueryAnalysisService(AggregationOptimizer aggregationOptimizer,
                                ScriptQueryOptimizer scriptQueryOptimizer,
                                WildcardQueryOptimizer wildcardQueryOptimizer,
                                LargeSizeQueryOptimizer largeSizeQueryOptimizer) {
        this.aggregationOptimizer = aggregationOptimizer;
        this.scriptQueryOptimizer = scriptQueryOptimizer;
        this.wildcardQueryOptimizer = wildcardQueryOptimizer;
        this.largeSizeQueryOptimizer = largeSizeQueryOptimizer;
    }

    public SlowQueryAnalysisResult analyzeAndOptimize(Map<String, Object> queryMap, String queryId) {
        SlowQueryAnalysisResult result = createBaseResult(queryMap, queryId);

        if (containsWildcardQuery(queryMap)) {
            return wildcardQueryOptimizer.optimize(result, queryMap);
        } else if (isLargeSizeQuery(queryMap)) {
            return largeSizeQueryOptimizer.optimize(result, queryMap);
        } else if (isComplexAggregation(queryMap)) {
            return aggregationOptimizer.optimize(result, queryMap);
        } else if (containsScriptQuery(queryMap)) {
            return scriptQueryOptimizer.optimize(result, queryMap);
        }

        setNoIssues(result);
        return result;
    }

    private SlowQueryAnalysisResult createBaseResult(Map<String, Object> queryMap, String queryId) {
        SlowQueryAnalysisResult result = new SlowQueryAnalysisResult();
        result.setQueryId(queryId);
        result.setOriginalQuery(JSON.toJSONString(queryMap));
        result.setOriginalExecutionTime(estimateExecutionTime(queryMap));
        return result;
    }

    private void setNoIssues(SlowQueryAnalysisResult result) {
        result.setIssueType("未知");
        result.setImpactLevel("medium");
        result.setExplanation("未发现明显性能问题");
        result.setOptimizedExecutionTime(result.getOriginalExecutionTime());
        result.setOptimizedQuery(result.getOriginalQuery());
    }

    private long estimateExecutionTime(Map<String, Object> queryMap) {
        if (containsWildcardQuery(queryMap)) return 8000;
        if (isLargeSizeQuery(queryMap)) return 6000;
        if (isComplexAggregation(queryMap)) return 10000;
        return 1000;
    }

    /**
     * 判断是否是通配符前缀查询
     */
    @SuppressWarnings("unchecked")
    private boolean containsWildcardQuery(Map<String, Object> queryMap) {
        if (queryMap.containsKey("query")) {
            Map<String, Object> query = (Map<String, Object>) queryMap.get("query");
            if (query.containsKey("wildcard")) {
                Map<String, Object> wildcard = (Map<String, Object>) query.get("wildcard");
                for (Object val : wildcard.values()) {
                    if (val instanceof String && ((String) val).startsWith("*")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断是否是大结果集查询
     */
    private boolean isLargeSizeQuery(Map<String, Object> queryMap) {
        return queryMap.containsKey("size") &&
                (queryMap.get("size") instanceof Integer && (Integer) queryMap.get("size") > 5000);
    }

    /**
     * 判断是否是复杂聚合查询
     */
    private boolean isComplexAggregation(Map<String, Object> queryMap) {
        return queryMap.containsKey("aggs") || queryMap.containsKey("aggregations");
    }

    /**
     * 判断是否包含script查询
     */
    @SuppressWarnings("unchecked")
    private boolean containsScriptQuery(Map<String, Object> queryMap) {
        if (queryMap.containsKey("query")) {
            Map<String, Object> query = (Map<String, Object>) queryMap.get("query");
            return query.containsKey("script") ||
                    (query.containsKey("function_score") &&
                            ((Map<String, Object>)query.get("function_score")).containsKey("script_score"));
        }
        return false;
    }
}