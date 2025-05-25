package com.dipper.monitor.controller.elastic.thread;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.entity.elastic.thread.check.pool.ThreadPoolTrendResult;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadCheckResult;
import com.dipper.monitor.entity.elastic.thread.hot.ThreadHotView;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadCharReq;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadChartSummary;
import com.dipper.monitor.service.elastic.thread.ThreadManagerService;
import com.dipper.monitor.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/thread_manager")
@Tag(name = "线程管理", description = "线程管理")
public class ThreadManagerController {

    @Autowired
    private ThreadManagerService threadManagerService;




    @GetMapping("/threadPage")
    @Operation(summary = "分页查询热点线程", description = "分页查询热点线程")
    public JSONObject threadPage() {
        try {
            List<ThreadHotView> pageResult = threadManagerService.threadPage();
            return ResultUtils.onSuccess(pageResult);
        } catch (Exception e) {
            log.error("分页查询热点线程失败", e);
            return ResultUtils.onFail(500, "分页查询热点线程失败: " + e.getMessage());
        }
    }



    @GetMapping("/threadDetail")
    @Operation(summary = "获取线程详情", description = "获取线程详情")
    public JSONObject threadDetail(@RequestParam Integer threadId) {
        try {
            ThreadHotView thread = threadManagerService.getThreadDetail(threadId);
            if (thread == null) {
                return ResultUtils.onFail(404, "未找到指定线程");
            }
            return ResultUtils.onSuccess(thread);
        } catch (Exception e) {
            log.error("获取线程详情失败", e);
            return ResultUtils.onFail(500, "获取线程详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/refreshThreadList")
    @Operation(summary = "刷新线程列表", description = "刷新线程列表")
    public JSONObject refreshThreadList() {
        try {
            List<ThreadHotView> threads = threadManagerService.refreshThreadList();
            return ResultUtils.onSuccess(threads);
        } catch (Exception e) {
            log.error("刷新线程列表失败", e);
            return ResultUtils.onFail(500, "刷新线程列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/threadChart")
    @Operation(summary = "获取线走势图", description = "获取线走势图")
    public JSONObject threadChart(@RequestBody ThreadCharReq threadCharReq) {
        try {
            List<ThreadMetricEntity> threads = threadManagerService.getThreadMetrics(threadCharReq);
            return ResultUtils.onSuccess(threads);
        } catch (Exception e) {
            log.error("刷新线程列表失败", e);
            return ResultUtils.onFail(500, "刷新线程列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/threadChartSummary")
    @Operation(summary = "获取线走势图一段时间内的统计信息", description = "获取线走势图一段时间内的统计信息")
    public JSONObject threadChartSummary(@RequestBody ThreadCharReq threadCharReq) {
        try {
            List<ThreadChartSummary> threads = threadManagerService.threadChartSummary(threadCharReq);
            return ResultUtils.onSuccess(threads);
        } catch (Exception e) {
            log.error("刷新线程列表失败", e);
            return ResultUtils.onFail(500, "刷新线程列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/threadPoolCheck")
    @Operation(summary = "线程环境检测", description = "执行线程环境检测并返回检测结果")
    public JSONObject threadPoolCheck() {
        try {
            List<ThreadPoolTrendResult> result = threadManagerService.threadPoolCheck();
            return ResultUtils.onSuccess(result);
        } catch (Exception e) {
            log.error("线程环境检测失败", e);
            return ResultUtils.onFail(500, "线程环境检测失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/threadRealTimeCheck")
    @Operation(summary = "线程环境检测", description = "执行线程环境检测并返回检测结果")
    public JSONObject threadRealTimeCheck() {
        try {
            ThreadCheckResult result = threadManagerService.threadRealTimeCheck();
            return ResultUtils.onSuccess(result);
        } catch (Exception e) {
            log.error("线程环境检测失败", e);
            return ResultUtils.onFail(500, "线程环境检测失败: " + e.getMessage());
        }
    }
}
