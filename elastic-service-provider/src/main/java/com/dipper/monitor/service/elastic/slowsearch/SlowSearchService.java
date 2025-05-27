package com.dipper.monitor.service.elastic.slowsearch;

import com.dipper.monitor.entity.elastic.slowsearch.slow.*;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryView;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.util.List;

public interface SlowSearchService {
    /**
     * 分页查询慢查询列表
     * @param pageReq 分页请求参数
     * @return 慢查询列表和总数
     */
    Tuple2<List<SlowQueryView>, Integer> slowSearchPage(SlowQueryPageReq pageReq) throws IOException;

    /**
     * 慢查询统计
     * @param slowQuerySummaryReq 慢查询统计请求参数
     * @return 慢查询统计结果
     */
    SlowQuerySummaryView slowSearchSummary(SlowQuerySummaryReq slowQuerySummaryReq);

    /**
     * 查询优化建议
     * @param queryOptimizationReq
     * @return
     */
    String queryOptimization(QueryOptimizationReq queryOptimizationReq);

    /**
     * 索引优化建议
     * @param queryOptimizationReq
     * @return
     */
    IndexSlowAnalysisResult indexOptimization(IndexOptimizationReq indexOptimizationReq);
}
