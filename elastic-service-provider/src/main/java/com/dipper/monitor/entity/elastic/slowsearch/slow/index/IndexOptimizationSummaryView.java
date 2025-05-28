package com.dipper.monitor.entity.elastic.slowsearch.slow.index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 索引优化统计视图
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexOptimizationSummaryView {
    // 总索引数量
    private int totalIndices;
    // 总分析查询数量
    private int totalAnalyzedQueries;
    // 热点索引列表
    private List<IndexCount> hotIndices;
    // 问题类型统计
    private List<IssueTypeCount> issueTypeCounts;
    // 优化建议类型统计
    private List<OptimizationTypeCount> optimizationTypeCounts;
    // 索引优化详情
    private List<IndexOptimizationDetail> indexDetails;
    
    /**
     * 索引计数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndexCount {
        private String name;
        private int value;
    }
    
    /**
     * 问题类型计数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueTypeCount {
        private String type;
        private int count;
    }
    
    /**
     * 优化建议类型计数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationTypeCount {
        private String type;
        private int count;
    }
    
    /**
     * 索引优化详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndexOptimizationDetail {
        // 索引名称
        private String indexName;
        // 索引设置优化建议
        private List<IndexSettingOptimization> settingOptimizations;
        // 查询问题类型统计
        private Map<String, Integer> issueTypeCounts;
    }
}