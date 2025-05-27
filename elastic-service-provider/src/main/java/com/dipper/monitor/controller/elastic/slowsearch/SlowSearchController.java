package com.dipper.monitor.controller.elastic.slowsearch;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.slowsearch.slow.QueryOptimizationReq;
import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQueryPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryView;
import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQuerySummaryReq;
import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQuerySummaryView;
import com.dipper.monitor.service.elastic.slowsearch.SlowSearchService;
import com.dipper.monitor.utils.ResultUtils;
import com.dipper.monitor.utils.Tuple2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/slow_search")
@Tag(name = "慢查询相关", description = "慢查询相关")
public class SlowSearchController {

    @Autowired
    private SlowSearchService slowSearchService;

    @PostMapping("/slowSearchPage")
    @Operation(summary = "分页查询慢查询", description = "分页查询慢查询")
    public JSONObject slowSearchPage(@RequestBody SlowQueryPageReq pageReq) {
        try {
            Tuple2<List<SlowQueryView>,Integer> pageResult = slowSearchService.slowSearchPage(pageReq);
            List<SlowQueryView> k = pageResult.getK();
            Integer v = pageResult.getV();
            return  ResultUtils.onSuccessWithPageTotal(v,k);
        } catch (Exception e) {
            log.error("分页查询策略失败", e);
            return ResultUtils.onFail(500, "分页查询策略失败: " + e.getMessage());
        }
    }

    @PostMapping("/queryOptimization")
    @Operation(summary = "单个查询优化", description = "单个查询优化")
    public JSONObject queryOptimization(@RequestBody QueryOptimizationReq queryOptimizationReq) {
        try {
            String pageResult = slowSearchService.queryOptimization(queryOptimizationReq);
            return  ResultUtils.onSuccess(pageResult);
        } catch (Exception e) {
            log.error("分页查询策略失败", e);
            return ResultUtils.onFail(500, "分页查询策略失败: " + e.getMessage());
        }
    }

    @PostMapping("/indexOptimization")
    @Operation(summary = "针对index的查询优化", description = "针对index的查询优化")
    public JSONObject indexOptimization(@RequestBody QueryOptimizationReq queryOptimizationReq) {
        try {
            String pageResult = slowSearchService.indexOptimization(queryOptimizationReq);
            return  ResultUtils.onSuccess(pageResult);
        } catch (Exception e) {
            log.error("分页查询策略失败", e);
            return ResultUtils.onFail(500, "分页查询策略失败: " + e.getMessage());
        }
    }




    @PostMapping("/slowSearchSummary")
    @Operation(summary = "慢查询统计", description = "慢查询统计")
    public JSONObject slowSearchSummary(@RequestBody SlowQuerySummaryReq slowQuerySummaryReq) {
        try {
            SlowQuerySummaryView slowQuerySummaryView = slowSearchService.slowSearchSummary(slowQuerySummaryReq);
            return  ResultUtils.onSuccess(slowQuerySummaryView);
        } catch (Exception e) {
            log.error("慢查询统计失败", e);
            return ResultUtils.onFail(500, "慢查询统计失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/queryAnalysis")
    @Operation(summary = "慢查询分析", description = "获取慢查询分析统计数据")
    public JSONObject queryAnalysis(@RequestBody SlowQuerySummaryReq slowQuerySummaryReq) {
        try {
            SlowQuerySummaryView slowQuerySummaryView = slowSearchService.slowSearchSummary(slowQuerySummaryReq);
            return ResultUtils.onSuccess(slowQuerySummaryView);
        } catch (Exception e) {
            log.error("获取慢查询分析数据失败", e);
            return ResultUtils.onFail(500, "获取慢查询分析数据失败: " + e.getMessage());
        }
    }
}
