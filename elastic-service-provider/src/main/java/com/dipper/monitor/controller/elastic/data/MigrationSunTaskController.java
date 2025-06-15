package com.dipper.monitor.controller.elastic.data;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskReq;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskView;
import com.dipper.monitor.entity.elastic.data.migration.sun.SunRunTaskReq;
import com.dipper.monitor.entity.elastic.data.migration.sun.SunTaskView;
import com.dipper.monitor.service.elastic.data.MigrationParentTaskService;
import com.dipper.monitor.service.elastic.data.MigrationSunTaskService;
import com.dipper.monitor.utils.ResultUtils;
import com.dipper.monitor.utils.Tuple2;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/migration_sun")
public class MigrationSunTaskController {


    @Autowired
    private MigrationSunTaskService migrationSunTaskService;

    @PostMapping("/page")
    @Operation(summary = "分页查询任务", description = "分页查询任务列表")
    public JSONObject getSunTaskByPage(@RequestBody PageReq pageReq) {
        try {
            Tuple2<List<SunTaskView>,Long> pageResult = migrationSunTaskService.getSunTaskByPage(pageReq);
            List<SunTaskView> k = pageResult.getK();
            Long v = pageResult.getV();
            return  ResultUtils.onSuccessWithPageTotal(v,k);
        } catch (Exception e) {
            log.error("分页查询任务失败", e);
            return ResultUtils.onFail(500, "分页查询任务失败: " + e.getMessage());
        }
    }


    @PostMapping("/run_task")
    @Operation(summary = "运行任务", description = "运行任务")
    public JSONObject runTask(@RequestBody SunRunTaskReq sunRunTaskReq) {
        try {
            migrationSunTaskService.runTask(sunRunTaskReq);
            return  ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.warn("运行任务错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("运行任务失败", e);
            return ResultUtils.onFail(500, "运行任务失败: " + e.getMessage());
        }
    }




}
