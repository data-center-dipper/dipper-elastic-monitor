package com.dipper.monitor.entity.elastic.slowsearch.slow;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 慢查询统计视图
 * 对应前端慢查询分析页面所需数据
 */
@Data
public class SlowQuerySummaryView {
    // 基础统计数据
    private Integer totalQueries;       // 慢查询总数
    private Long avgExecutionTime;      // 平均执行时间(ms)
    private Long maxExecutionTime;      // 最长执行时间(ms)
    private Integer killedQueriesCount; // 已终止查询数
    
    // 查询类型分布 - 饼图数据
    private List<TypeDistribution> queryTypeDistribution;
    
    // 执行时间分布 - 柱状图数据
    private List<Integer> executionTimeDistribution; // 对应前端的[3-5s, 5-10s, 10-30s, 30-60s, >60s]区间
    
    // 热点索引 - 柱状图数据
    private List<IndexCount> hotIndices;
    
    // 查询趋势 - 折线图数据
    private QueryTrend queryTrend;
    
    // 查询类型分布项
    @Data
    public static class TypeDistribution {
        private Integer value; // 数量
        private String name;   // 类型名称
    }
    
    // 索引计数项
    @Data
    public static class IndexCount {
        private String name;  // 索引名称
        private Integer value; // 查询次数
    }
    
    // 查询趋势数据
    @Data
    public static class QueryTrend {
        private List<String> timePoints;   // 时间点
        private List<Integer> slowQueries; // 慢查询数
        private List<Integer> killedQueries; // 已终止查询数
    }
}
