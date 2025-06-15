package com.dipper.monitor.controller.elastic.data;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskReq;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskView;
import com.dipper.monitor.service.elastic.data.MigrationParentTaskService;
import com.dipper.monitor.utils.ResultUtils;
import com.dipper.monitor.utils.Tuple2;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/migration_parent")
public class MigrationParentTaskController {

    
    @Autowired
    private MigrationParentTaskService migrationParentTaskService;

    @PostMapping("/page")
    @Operation(summary = "分页查询任务", description = "分页查询任务列表")
    public JSONObject getMigrationTaskByPage(@RequestBody PageReq pageReq) {
        try {
            Tuple2<List<MigrationTaskView>,Long> pageResult = migrationParentTaskService.getPoliciesByPage(pageReq);
            List<MigrationTaskView> k = pageResult.getK();
            Long v = pageResult.getV();
            return  ResultUtils.onSuccessWithPageTotal(v,k);
        } catch (Exception e) {
            log.error("分页查询任务失败", e);
            return ResultUtils.onFail(500, "分页查询任务失败: " + e.getMessage());
        }
    }

    @PostMapping("/add_task")
    @Operation(summary = "添加任务", description = "添加任务")
    public JSONObject addTask(@RequestBody MigrationTaskReq migrationTaskReq) {
        try {
            migrationParentTaskService.addTask(migrationTaskReq);
            return ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.warn("添加任务: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("添加任务", e);
            return ResultUtils.onFail(500, "添加任务: " + e.getMessage());
        }
    }

    @PostMapping("/update_task")
    @Operation(summary = "更新任务", description = "更新任务")
    public JSONObject updateTask(@RequestBody MigrationTaskReq migrationTaskReq) {
        try {
            migrationParentTaskService.updateTask(migrationTaskReq);
            return  ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.warn("更新任务错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新任务失败", e);
            return ResultUtils.onFail(500, "更新任务失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete_task")
    @Operation(summary = "删除任务", description = "根据ID删除任务")
    public JSONObject deleteTask(@PathVariable("id") Integer id) {
        try {
             migrationParentTaskService.deleteTask(id);
            return  ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.warn("删除任务参数错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("删除任务失败", e);
            return ResultUtils.onFail(500, "删除任务失败: " + e.getMessage());
        }
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取任务详情", description = "根据ID获取任务详情")
    public JSONObject getOneTask(@PathVariable("id") Integer id) {
        try {
            MigrationTaskView response = migrationParentTaskService.getOneTask(id);
            if (response == null) {
                return ResultUtils.onFail(404, "任务不存在");
            }
            return  ResultUtils.onSuccess(response);
        } catch (IllegalArgumentException e) {
            log.warn("获取任务参数错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("获取任务失败", e);
            return ResultUtils.onFail(500, "获取任务失败: " + e.getMessage());
        }
    }



}
