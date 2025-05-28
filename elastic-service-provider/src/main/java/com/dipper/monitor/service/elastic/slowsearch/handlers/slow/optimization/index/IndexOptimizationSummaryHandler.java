package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.slowsearch.slow.index.*;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.slowsearch.RealSlowSearchService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 索引优化统计处理器 - 针对所有索引的查询优化统计
 */
@Service
public class IndexOptimizationSummaryHandler {

    private final ElasticClientService elasticClientService;
    private final ElasticRealIndexService elasticRealIndexService;
    private final RealSlowSearchService realSlowSearchService;
    private final SlowQueryStoreService slowQueryStoreService;

    private  QueryAnalysisService queryAnalysisService;
    private  IndexSettingAnalyzer indexSettingAnalyzer;
    private  QueryPatternAnalyzer queryPatternAnalyzer;

    public IndexOptimizationSummaryHandler(ElasticClientService elasticClientService,
                                           ElasticRealIndexService elasticRealIndexService,
                                           RealSlowSearchService realSlowSearchService,
                                           SlowQueryStoreService slowQueryStoreService) {
        this.elasticClientService = elasticClientService;
        this.elasticRealIndexService = elasticRealIndexService;
        this.realSlowSearchService = realSlowSearchService;
        this.slowQueryStoreService = slowQueryStoreService;
    }



    /**
     * 获取所有索引的查询优化统计
     */
    public IndexOptimizationSummaryView getIndexOptimizationSummary(IndexOptimizationReq indexOptimizationReq) {
        // 获取时间范围内的慢查询记录
        // 获取当前时间
        // 获取当前时间往前推3个小时
        // 获取当前时间（endTime）
        Date endTime = new Date();

        // 创建 Calendar 实例，设置为当前时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        // 往前推3小时
        calendar.add(Calendar.HOUR_OF_DAY, -3);
        Date startTime = calendar.getTime();



        List<SlowQueryEntity> slowQueries = slowQueryStoreService.queryByTimeRange(startTime, endTime);
        
        // 获取所有相关索引
        Set<String> indexNames = extractIndexNames(slowQueries);
        
        // 分析每个索引的设置
        Map<String, List<IndexSettingOptimization>> indexSettingOptimizations = analyzeIndexSettings(indexNames);
        
        // 分析所有慢查询
        List<SlowQueryAnalysisResult> queryAnalysisResults = analyzeQueries(slowQueries);
        
        // 按索引分组统计查询问题
        Map<String, Map<String, Integer>> indexIssueTypeCounts = countIssueTypesByIndex(queryAnalysisResults, slowQueries);
        
        // 统计总体查询问题类型分布
        Map<String, Integer> overallIssueTypeCounts = countOverallIssueTypes(queryAnalysisResults);
        
        // 统计热点索引
        List<IndexOptimizationSummaryView.IndexCount> hotIndices = getHotIndices(slowQueries);
        
        // 统计优化建议分布
        List<IndexOptimizationSummaryView.OptimizationTypeCount> optimizationTypeCounts = 
                countOptimizationTypes(indexSettingOptimizations, queryAnalysisResults);
        
        // 构建返回结果
        return buildSummaryResult(indexNames.size(), queryAnalysisResults.size(), 
                hotIndices, overallIssueTypeCounts, optimizationTypeCounts, 
                indexSettingOptimizations, indexIssueTypeCounts);
    }
    
    /**
     * 从慢查询记录中提取所有索引名称
     */
    private Set<String> extractIndexNames(List<SlowQueryEntity> slowQueries) {
        Set<String> indexNames = new HashSet<>();
        
        for (SlowQueryEntity query : slowQueries) {
            if (query.getIndexName() != null && !query.getIndexName().isEmpty()) {
                // 处理可能包含多个索引的情况（以逗号分隔）
                String[] indices = query.getIndexName().split(",");
                for (String index : indices) {
                    String trimmedIndex = index.trim();
                    if (!trimmedIndex.isEmpty()) {
                        indexNames.add(trimmedIndex);
                    }
                }
            }
        }
        
        return indexNames;
    }
    
    /**
     * 分析所有索引的设置
     */
    private Map<String, List<IndexSettingOptimization>> analyzeIndexSettings(Set<String> indexNames) {
        Map<String, List<IndexSettingOptimization>> result = new HashMap<>();
        
        for (String indexName : indexNames) {
            JSONObject setting = elasticRealIndexService.getIndexSetting(indexName);
            List<IndexSettingOptimization> optimizations = indexSettingAnalyzer.analyze(setting);
            
            // 为每个优化设置索引名称
            for (IndexSettingOptimization opt : optimizations) {
                opt.setIndexName(indexName);
            }
            
            result.put(indexName, optimizations);
        }
        
        return result;
    }
    
    /**
     * 分析所有慢查询
     */
    private List<SlowQueryAnalysisResult> analyzeQueries(List<SlowQueryEntity> slowQueries) {
        return slowQueries.stream()
                .filter(query -> query.getQueryContent() != null && !query.getQueryContent().isEmpty())
                .map(this::processSingleQuery)
                .collect(Collectors.toList());
    }
    
    /**
     * 处理单个查询
     */
    private SlowQueryAnalysisResult processSingleQuery(SlowQueryEntity entity) {
        Map<String, Object> queryMap = JSONObject.parseObject(entity.getQueryContent());
        SlowQueryAnalysisResult result = queryAnalysisService.analyzeAndOptimize(queryMap, entity.getId()+"");
        result.setIndexName(entity.getIndexName()); // 设置索引名称，便于后续按索引分组
        return result;
    }
    
    /**
     * 按索引分组统计查询问题类型
     */
    private Map<String, Map<String, Integer>> countIssueTypesByIndex(
            List<SlowQueryAnalysisResult> analysisResults, 
            List<SlowQueryEntity> slowQueries) {
        
        // 创建查询ID到索引名称的映射
        Map<Long, String> queryIdToIndexMap = new HashMap<>();
        for (SlowQueryEntity query : slowQueries) {
            if (query.getId() != null && query.getIndexName() != null) {
                queryIdToIndexMap.put(query.getId(), query.getIndexName());
            }
        }
        
        // 按索引分组统计问题类型
        Map<String, Map<String, Integer>> result = new HashMap<>();
        
        for (SlowQueryAnalysisResult analysis : analysisResults) {
            String indexName = queryIdToIndexMap.getOrDefault(analysis.getQueryId(), "unknown");
            String issueType = analysis.getIssueType();
            
            if (!result.containsKey(indexName)) {
                result.put(indexName, new HashMap<>());
            }
            
            Map<String, Integer> issueTypeCounts = result.get(indexName);
            issueTypeCounts.put(issueType, issueTypeCounts.getOrDefault(issueType, 0) + 1);
        }
        
        return result;
    }
    
    /**
     * 统计总体查询问题类型分布
     */
    private Map<String, Integer> countOverallIssueTypes(List<SlowQueryAnalysisResult> analysisResults) {
        return queryPatternAnalyzer.analyzePatterns(analysisResults);
    }
    
    /**
     * 获取热点索引（慢查询最多的前10个索引）
     */
    private List<IndexOptimizationSummaryView.IndexCount> getHotIndices(List<SlowQueryEntity> slowQueries) {
        Map<String, Integer> indexCountMap = new HashMap<>();
        
        for (SlowQueryEntity entity : slowQueries) {
            if (entity.getIndexName() == null || entity.getIndexName().isEmpty()) continue;
            
            // 处理可能包含多个索引的情况（以逗号分隔）
            String[] indices = entity.getIndexName().split(",");
            for (String index : indices) {
                String trimmedIndex = index.trim();
                if (!trimmedIndex.isEmpty()) {
                    indexCountMap.put(trimmedIndex, indexCountMap.getOrDefault(trimmedIndex, 0) + 1);
                }
            }
        }
        
        // 获取前10个热点索引
        return indexCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    IndexOptimizationSummaryView.IndexCount indexCount = new IndexOptimizationSummaryView.IndexCount();
                    indexCount.setName(entry.getKey());
                    indexCount.setValue(entry.getValue());
                    return indexCount;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 统计优化建议类型分布
     */
    private List<IndexOptimizationSummaryView.OptimizationTypeCount> countOptimizationTypes(
            Map<String, List<IndexSettingOptimization>> indexSettingOptimizations,
            List<SlowQueryAnalysisResult> queryAnalysisResults) {
        
        // 统计索引设置优化类型
        Map<String, Integer> settingOptTypes = new HashMap<>();
        for (List<IndexSettingOptimization> optimizations : indexSettingOptimizations.values()) {
            for (IndexSettingOptimization opt : optimizations) {
                String settingName = opt.getSettingName();
                settingOptTypes.put(settingName, settingOptTypes.getOrDefault(settingName, 0) + 1);
            }
        }
        
        // 统计查询优化类型
        Map<String, Integer> queryOptTypes = new HashMap<>();
        for (SlowQueryAnalysisResult analysis : queryAnalysisResults) {
            String issueType = analysis.getIssueType();
            if (!"未知".equals(issueType)) {
                queryOptTypes.put(issueType, queryOptTypes.getOrDefault(issueType, 0) + 1);
            }
        }
        
        // 合并所有优化类型
        Map<String, Integer> allOptTypes = new HashMap<>();
        allOptTypes.putAll(settingOptTypes);
        allOptTypes.putAll(queryOptTypes);
        
        // 转换为视图对象
        return allOptTypes.entrySet().stream()
                .map(entry -> {
                    IndexOptimizationSummaryView.OptimizationTypeCount count = 
                            new IndexOptimizationSummaryView.OptimizationTypeCount();
                    count.setType(entry.getKey());
                    count.setCount(entry.getValue());
                    return count;
                })
                .sorted(Comparator.comparing(IndexOptimizationSummaryView.OptimizationTypeCount::getCount).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * 构建统计结果
     */
    private IndexOptimizationSummaryView buildSummaryResult(
            int totalIndices,
            int totalAnalyzedQueries,
            List<IndexOptimizationSummaryView.IndexCount> hotIndices,
            Map<String, Integer> overallIssueTypeCounts,
            List<IndexOptimizationSummaryView.OptimizationTypeCount> optimizationTypeCounts,
            Map<String, List<IndexSettingOptimization>> indexSettingOptimizations,
            Map<String, Map<String, Integer>> indexIssueTypeCounts) {
        
        IndexOptimizationSummaryView result = new IndexOptimizationSummaryView();
        result.setTotalIndices(totalIndices);
        result.setTotalAnalyzedQueries(totalAnalyzedQueries);
        result.setHotIndices(hotIndices);
        
        // 转换问题类型统计为视图对象
        List<IndexOptimizationSummaryView.IssueTypeCount> issueTypeCounts = 
                overallIssueTypeCounts.entrySet().stream()
                .map(entry -> {
                    IndexOptimizationSummaryView.IssueTypeCount count = 
                            new IndexOptimizationSummaryView.IssueTypeCount();
                    count.setType(entry.getKey());
                    count.setCount(entry.getValue());
                    return count;
                })
                .sorted(Comparator.comparing(IndexOptimizationSummaryView.IssueTypeCount::getCount).reversed())
                .collect(Collectors.toList());
        
        result.setIssueTypeCounts(issueTypeCounts);
        result.setOptimizationTypeCounts(optimizationTypeCounts);
        
        // 构建索引优化详情
        List<IndexOptimizationSummaryView.IndexOptimizationDetail> indexDetails = new ArrayList<>();
        
        for (String indexName : indexSettingOptimizations.keySet()) {
            IndexOptimizationSummaryView.IndexOptimizationDetail detail = 
                    new IndexOptimizationSummaryView.IndexOptimizationDetail();
            detail.setIndexName(indexName);
            detail.setSettingOptimizations(indexSettingOptimizations.get(indexName));
            detail.setIssueTypeCounts(indexIssueTypeCounts.getOrDefault(indexName, Collections.emptyMap()));
            indexDetails.add(detail);
        }
        
        result.setIndexDetails(indexDetails);
        
        return result;
    }
}