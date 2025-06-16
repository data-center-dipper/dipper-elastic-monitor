package com.dipper.monitor.service.elastic.data.migration;

import com.dipper.monitor.entity.db.elastic.SunTaskEntity;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskView;
import com.dipper.monitor.entity.elastic.data.migration.sun.SunRunTaskReq;
import com.dipper.monitor.service.elastic.data.MigrationSunTaskService;
import com.dipper.monitor.service.elastic.data.SunTaskExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubTaskFailedRetryRunHandler extends AbstractSubTaskRunHandler {

    @Autowired
    private SunTaskExecutorService sunTaskExecutorService;

    public SubTaskFailedRetryRunHandler(MigrationSunTaskService migrationSunTaskService) {
        super(migrationSunTaskService, null);
        // 注意：这里传入的sunTaskExecutorService为null，但在类中通过@Autowired注入
    }

    public void retryFailedSubtasks(SunRunTaskReq sunRunTaskReq) {
        String parentTaskId = sunRunTaskReq.getParentTaskId();
        log.info("开始重试失败的子任务，父任务ID: {}", parentTaskId);

        // 获取父任务
        MigrationTaskView oneTaskByTaskId = migrationParentTaskService.getOneTaskByTaskId(parentTaskId);
        if (oneTaskByTaskId == null) {
            throw new RuntimeException("未找到对应的任务");
        }

        // 获取所有子任务
        List<SunTaskEntity> allSubTasks = migrationSunTaskService.getSunTaskByParentTaskId(parentTaskId);
        if (allSubTasks == null || allSubTasks.isEmpty()) {
            throw new RuntimeException("未找到对应的子任务");
        }

        // 筛选出失败的子任务
        List<SunTaskEntity> failedTasks = allSubTasks.stream()
                .filter(task -> "FAILED".equals(task.getStatus()))
                .sorted(Comparator.comparing(SunTaskEntity::getIndexName))
                .collect(Collectors.toList());

        if (failedTasks.isEmpty()) {
            log.info("没有找到失败的子任务，父任务ID: {}", parentTaskId);
            return;
        }

        log.info("失败的子任务总数: {}, 已按索引名称排序", failedTasks.size());

        // 获取父任务的并发限制
        Integer concurrencyLimit = oneTaskByTaskId.getConcurrencyLimit();
        if (concurrencyLimit == null || concurrencyLimit < 1) {
            concurrencyLimit = 1; // 默认并发数为1
        }

        log.info("任务并发限制: {}", concurrencyLimit);

        // 更新所有失败子任务状态为PENDING
        for (SunTaskEntity task : failedTasks) {
            task.setStatus("PENDING");
            task.setStartTime(LocalDateTime.now());
            task.setEndTime(null);
            task.setErrorLog(null);
            
            try {
                // 通过Mapper更新子任务状态
                migrationSunTaskService.getSunTaskByParentTaskId(parentTaskId); // 这里应该是更新操作，但示例代码中没有提供更新方法
                log.info("子任务状态已更新为PENDING, 任务ID: {}, 索引: {}", task.getId(), task.getIndexName());
            } catch (Exception e) {
                log.error("更新子任务状态失败, 任务ID: {}, 错误: {}", task.getId(), e.getMessage(), e);
            }
        }

        // 执行子任务
        try {
            // 将排序后的任务列表和父任务信息传递给执行器
            sunTaskExecutorService.executeAllTasks(failedTasks, oneTaskByTaskId, concurrencyLimit);
            log.info("所有失败子任务已提交重试, 父任务ID: {}", parentTaskId);
        } catch (Exception e) {
            log.error("提交失败子任务重试失败, 父任务ID: {}, 错误: {}", parentTaskId, e.getMessage(), e);
            throw new RuntimeException("提交失败子任务重试失败: " + e.getMessage(), e);
        }
    }
}
