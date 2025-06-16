package com.dipper.monitor.service.elastic.data.migration;

import com.dipper.monitor.entity.db.elastic.SunTaskEntity;
import com.dipper.monitor.entity.elastic.data.migration.sun.SunRunTaskReq;
import com.dipper.monitor.service.elastic.data.MigrationSunTaskService;
import com.dipper.monitor.service.elastic.data.SunTaskExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SubTaskRunHandler extends AbstractSubTaskRunHandler {

    @Autowired
    private SunTaskExecutorService sunTaskExecutorService;

    public SubTaskRunHandler(MigrationSunTaskService migrationSunTaskService) {
        super(migrationSunTaskService, null);
        // 注意：这里传入的sunTaskExecutorService为null，但在类中通过@Autowired注入
    }

    public void runOneSubtask(SunRunTaskReq sunRunTaskReq) {
        String subTaskId = sunRunTaskReq.getSubTaskId();
        log.info("开始执行单个子任务，子任务ID: {}", subTaskId);
        
        if (subTaskId == null || subTaskId.isEmpty()) {
            throw new RuntimeException("子任务ID不能为空");
        }
        
        // 获取子任务
        SunTaskEntity task = migrationSunTaskService.getSunTaskById(Long.valueOf(subTaskId));
        if (task == null) {
            throw new RuntimeException("未找到对应的子任务，ID: " + subTaskId);
        }
        
        log.info("找到子任务，ID: {}, 索引: {}, 当前状态: {}", task.getId(), task.getIndexName(), task.getStatus());
        
        // 执行子任务
        try {
            sunTaskExecutorService.executeTask(task);
            log.info("子任务执行完成，子任务ID: {}", subTaskId);
        } catch (Exception e) {
            log.error("子任务执行失败，子任务ID: {}, 错误: {}", subTaskId, e.getMessage(), e);
            throw new RuntimeException("子任务执行失败: " + e.getMessage(), e);
        }
    }
}
