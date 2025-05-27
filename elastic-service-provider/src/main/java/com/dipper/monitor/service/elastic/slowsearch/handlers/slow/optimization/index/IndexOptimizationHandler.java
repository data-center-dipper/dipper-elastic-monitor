package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.slowsearch.slow.*;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.slowsearch.RealSlowSearchService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;
import com.dipper.monitor.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IndexOptimizationHandler {

    private final ElasticClientService elasticClientService;
    private final ElasticRealIndexService elasticRealIndexService;
    private final RealSlowSearchService realSlowSearchService;
    private final SlowQueryStoreService slowQueryStoreService;

    public IndexOptimizationHandler(ElasticClientService elasticClientService,
                                    ElasticRealIndexService elasticRealIndexService,
                                    RealSlowSearchService realSlowSearchService,
                                    SlowQueryStoreService slowQueryStoreService) {
        this.elasticClientService = elasticClientService;
        this.elasticRealIndexService = elasticRealIndexService;
        this.realSlowSearchService = realSlowSearchService;
        this.slowQueryStoreService = slowQueryStoreService;
    }

    /**
     * 对指定索引进行慢查询分析并生成优化建议
     */
    public IndexSlowAnalysisResult indexOptimization(IndexOptimizationReq req) {
        String indexName = req.getIndexName();

        // Step 1: 获取索引的 setting 和 mapping
        JSONObject setting = elasticRealIndexService.getIndexSetting(indexName);
        JSONObject mappings = elasticRealIndexService.getMappingByIndexName(indexName);

        // Step 2: 获取最近 N 小时的慢查询记录
        List<SlowQueryEntity> slowQueries = slowQueryStoreService.queryByTimeRange();

        // Step 3: 分析每个慢查询，生成优化建议
        List<IndexSlowAnalysisResult> analysisResults = new ArrayList<>();
        for (SlowQueryEntity entity : slowQueries) {
            if (entity.getQueryContent() == null || entity.getQueryContent().isEmpty()) {
                continue;
            }

            Map<String, Object> queryMap = JSONObject.parseObject(entity.getQueryContent());
            SlowQueryAnalysisResult result = analyzeAndOptimize(queryMap, entity.getQueryId());
            analysisResults.add(result);
        }

        // Step 4: 构建返回结果
        IndexSlowAnalysisResult overallResult = new IndexSlowAnalysisResult();
        overallResult.setIndexName(indexName);
        overallResult.setIndexSetting(setting);
        overallResult.setIndexMapping(mappings);
        overallResult.setSlowQueryResults(analysisResults);

        return overallResult;
    }

    /**
     * 分析并优化单个查询
     */
    private SlowQueryAnalysisResult analyzeAndOptimize(Map<String, Object> queryMap, String queryId) {
        SlowQueryAnalysisResult result = new SlowQueryAnalysisResult();
        result.setQueryId(queryId);
        result.setOriginalQuery(JsonUtils.toJson(queryMap));
        result.setOriginalExecutionTime(estimateExecutionTime(queryMap));

        String issueType = "未知";
        String impactLevel = "medium";
        String explanation = "未发现明显性能问题";

        if (containsWildcardQuery(queryMap)) {
            issueType = "通配符前缀查询";
            impactLevel = "high";
            explanation = "通配符前缀查询无法使用倒排索引，会导致全表扫描，严重影响性能。建议改用 match_phrase 或 prefix 查询。";
            result.setOptimizedExecutionTime(1000L);
            result.setOptimizedQuery(JsonUtils.toJson(optimizeWildcardQuery(queryMap)));
        } else if (isLargeSizeQuery(queryMap)) {
            issueType = "大结果集查询";
            impactLevel = "medium";
            explanation = "返回大量结果会消耗内存和网络资源，建议减少 size 参数或使用 scroll API 分页查询。";
            result.setOptimizedExecutionTime(800L);
            result.setOptimizedQuery(JsonUtils.toJson(optimizeLargeSizeQuery(queryMap)));
        } else if (isComplexAggregation(queryMap)) {
            issueType = "复杂聚合查询";
            impactLevel = "high";
            explanation = "复杂聚合会显著增加 CPU 和内存开销，建议限制桶数量或在聚合前加 filter 过滤器。";
            result.setOptimizedExecutionTime(2000L);
            result.setOptimizedQuery(JsonUtils.toJson(optimizeAggregationQuery(queryMap)));
        } else {
            result.setOptimizedExecutionTime(result.getOriginalExecutionTime());
            result.setOptimizedQuery(result.getOriginalQuery());
        }

        result.setIssueType(issueType);
        result.setImpactLevel(impactLevel);
        result.setExplanation(explanation);

        return result;
    }

    /**
     * 判断是否是通配符前缀查询
     */
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
        return queryMap.containsKey("size") && (Integer) queryMap.get("size") > 5000;
    }

    /**
     * 判断是否是复杂聚合查询
     */
    private boolean isComplexAggregation(Map<String, Object> queryMap) {
        return queryMap.containsKey("aggs") || queryMap.containsKey("aggregations");
    }

    /**
     * 预估执行时间（模拟）
     */
    private long estimateExecutionTime(Map<String, Object> queryMap) {
        if (containsWildcardQuery(queryMap)) return 8000;
        if (isLargeSizeQuery(queryMap)) return 6000;
        if (isComplexAggregation(queryMap)) return 10000;
        return 1000;
    }

    /**
     * 替换 wildcard 查询为 match_phrase
     */
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

    /**
     * 优化大结果集查询：减少 size + 添加排序字段
     */
    private Map<String, Object> optimizeLargeSizeQuery(Map<String, Object> original) {
        Map<String, Object> optimized = new HashMap<>(original);
        optimized.put("size", 100);
        optimized.put("sort", Collections.singletonList(Collections.singletonMap("_id", "asc")));
        return optimized;
    }

    /**
     * 优化复杂聚合：添加过滤 + 限制桶数
     */
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