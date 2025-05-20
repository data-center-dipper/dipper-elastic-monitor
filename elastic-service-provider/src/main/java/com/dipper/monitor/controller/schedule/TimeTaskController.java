package com.dipper.monitor.controller.schedule;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.policy.PolicyPageRequest;
import com.dipper.monitor.entity.elastic.policy.response.LifePolicyResponse;
import com.dipper.monitor.entity.task.TaskListView;
import com.dipper.monitor.entity.task.TaskPageReq;
import com.dipper.monitor.service.schedule.TimeTaskService;
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
@RequestMapping("/dipper/monitor/api/v1/elastic/task_manager")
@Tag(name = "定时任务管理界面")
public class TimeTaskController {
    @Autowired
    private TimeTaskService timeTaskService;

    @PostMapping("/taskPage")
    @Operation(summary = "分页查询任务", description = "分页查询任务")
    public JSONObject taskPage(@RequestBody TaskPageReq taskPageReq) {
        try {
            Tuple2<List<TaskListView>,Long> pageResult = timeTaskService.taskPage(taskPageReq);
            List<TaskListView> k = pageResult.getK();
            Long v = pageResult.getV();
            return  ResultUtils.onSuccessWithPageTotal(v,k);
        } catch (Exception e) {
            log.error("分页查询策略失败", e);
            return ResultUtils.onFail(500, "分页查询策略失败: " + e.getMessage());
        }
    }


    @PostMapping("/taskStop")
    @Operation(summary = "停止任务", description = "停止任务")
    public JSONObject taskStop(@RequestParam Integer taskId) {
        try {
           timeTaskService.taskStop(taskId);

            return  ResultUtils.onSuccess();
        } catch (Exception e) {
            log.error("分页查询策略失败", e);
            return ResultUtils.onFail(500, "分页查询策略失败: " + e.getMessage());
        }
    }


    @PostMapping("/taskStart")
    @Operation(summary = "启动任务", description = "启动任务")
    public JSONObject taskStart(@RequestParam Integer taskId) {
        try {
            timeTaskService.taskStart(taskId);

            return  ResultUtils.onSuccess();
        } catch (Exception e) {
            log.error("分页查询策略失败", e);
            return ResultUtils.onFail(500, "分页查询策略失败: " + e.getMessage());
        }
    }

    @PostMapping("/taskExecute")
    @Operation(summary = "执行一次任务", description = "执行一次任务")
    public JSONObject taskExecute(@RequestParam Integer taskId) {
        try {
            timeTaskService.taskExecute(taskId);

            return  ResultUtils.onSuccess();
        } catch (Exception e) {
            log.error("分页查询策略失败", e);
            return ResultUtils.onFail(500, "分页查询策略失败: " + e.getMessage());
        }
    }

}
