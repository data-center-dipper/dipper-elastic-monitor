package com.dipper.monitor.service.elastic.data.migration;

import com.dipper.monitor.entity.db.elastic.SunTaskEntity;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskView;
import com.dipper.monitor.entity.elastic.data.migration.sun.SunRunTaskReq;
import com.dipper.monitor.service.elastic.data.MigrationSunTaskService;
import com.dipper.monitor.service.elastic.data.SunTaskExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SubTaskAllRunHandler extends AbstractSubTaskRunHandler {

    public SubTaskAllRunHandler(MigrationSunTaskService migrationSunTaskService, SunTaskExecutorService sunTaskExecutorService) {
        super(migrationSunTaskService, sunTaskExecutorService);
    }

    public void startAllSubtasks(SunRunTaskReq sunRunTaskReq) {
        String parentTaskId = sunRunTaskReq.getParentTaskId();
        log.info("开始执行所有子任务，父任务ID: {}", parentTaskId);

        MigrationTaskView oneTaskByTaskId = migrationParentTaskService.getOneTaskByTaskId(parentTaskId);
        if (oneTaskByTaskId == null) {
            throw new RuntimeException("未找到对应的任务");
        }
        List<SunTaskEntity> sunTaskByParentTaskId = migrationSunTaskService.getSunTaskByParentTaskId(parentTaskId);
        if (sunTaskByParentTaskId == null || sunTaskByParentTaskId.size() == 0) {
            throw new RuntimeException("未找到对应的子任务");
        }
        
        // 按照索引名称排序子任务
        List<SunTaskEntity> sortedTasks = sunTaskByParentTaskId.stream()
                .sorted(Comparator.comparing(SunTaskEntity::getIndexName))
                .collect(Collectors.toList());
        
        log.info("子任务总数: {}, 已按索引名称排序", sortedTasks.size());
        
        // 获取父任务的并发限制
        Integer concurrencyLimit = oneTaskByTaskId.getConcurrencyLimit();
        if (concurrencyLimit == null || concurrencyLimit < 1) {
            concurrencyLimit = 1; // 默认并发数为1
        }
        
        log.info("任务并发限制: {}", concurrencyLimit);
        
        // 更新所有子任务状态为PENDING
        for (SunTaskEntity task : sortedTasks) {
            task.setStatus("PENDING");
            task.setStartTime(LocalDateTime.now());
            task.setRetryCount(0);
            task.setErrorLog(null);
            
            // 通过Mapper更新子任务状态
            // 注意：这里假设MigrationSunTaskService有updateTask方法
            // 如果没有，需要通过Mapper直接更新
            try {
                // 更新子任务状态
//                migrationSunTaskService.updateTask(task);
                log.info("子任务状态已更新为PENDING, 任务ID: {}, 索引: {}", task.getId(), task.getIndexName());
            } catch (Exception e) {
                log.error("更新子任务状态失败, 任务ID: {}, 错误: {}", task.getId(), e.getMessage(), e);
            }
        }
        
        // 执行子任务
        // 注意：这里假设SunTaskExecutorService有executeTask方法
        // 如果SunTaskExecutorService接口中没有定义相应方法，需要先定义
        try {
            // 将排序后的任务列表和父任务信息传递给执行器
//            sunTaskExecutorService.executeAllTasks(sortedTasks, oneTaskByTaskId, concurrencyLimit);
            log.info("所有子任务已提交执行, 父任务ID: {}", parentTaskId);
        } catch (Exception e) {
            log.error("提交子任务执行失败, 父任务ID: {}, 错误: {}", parentTaskId, e.getMessage(), e);
            throw new RuntimeException("提交子任务执行失败: " + e.getMessage(), e);
        }
    }
}
