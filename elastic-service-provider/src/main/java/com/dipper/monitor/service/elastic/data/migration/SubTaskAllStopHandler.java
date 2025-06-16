package com.dipper.monitor.service.elastic.data.migration;

import com.dipper.monitor.entity.db.elastic.SunTaskEntity;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskView;
import com.dipper.monitor.entity.elastic.data.migration.sun.SunRunTaskReq;
import com.dipper.monitor.service.elastic.data.MigrationSunTaskService;
import com.dipper.monitor.mapper.MigrationSunTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SubTaskAllStopHandler extends AbstractSubTaskRunHandler {

    @Autowired
    private MigrationSunTaskMapper migrationSunTaskMapper;

    public SubTaskAllStopHandler(MigrationSunTaskService migrationSunTaskService) {
        super(migrationSunTaskService, null);
    }

    public void stopAllSubtasks(SunRunTaskReq sunRunTaskReq) {
        String parentTaskId = sunRunTaskReq.getParentTaskId();
        log.info("开始停止所有子任务，父任务ID: {}", parentTaskId);

        // 获取父任务
        MigrationTaskView oneTaskByTaskId = migrationParentTaskService.getOneTaskByTaskId(parentTaskId);
        if (oneTaskByTaskId == null) {
            throw new RuntimeException("未找到对应的任务");
        }

        // 获取所有子任务
        List<SunTaskEntity> sunTaskByParentTaskId = migrationSunTaskService.getSunTaskByParentTaskId(parentTaskId);
        if (sunTaskByParentTaskId == null || sunTaskByParentTaskId.isEmpty()) {
            throw new RuntimeException("未找到对应的子任务");
        }

        log.info("子任务总数: {}", sunTaskByParentTaskId.size());

        // 更新所有运行中的子任务状态为STOPPED
        int stoppedCount = 0;
        for (SunTaskEntity task : sunTaskByParentTaskId) {
            // 只停止状态为PENDING或RUNNING的任务
            if ("PENDING".equals(task.getStatus()) || "RUNNING".equals(task.getStatus())) {
                task.setStatus("STOPPED");
                task.setEndTime(LocalDateTime.now());
                
                try {
                    migrationSunTaskMapper.updateSubtaskStatus(task.getId(), task.getStatus());
                    stoppedCount++;
                    log.info("子任务状态已更新为STOPPED, 任务ID: {}, 索引: {}", task.getId(), task.getIndexName());
                } catch (Exception e) {
                    log.error("更新子任务状态失败, 任务ID: {}, 错误: {}", task.getId(), e.getMessage(), e);
                }
            }
        }

        log.info("停止子任务完成，父任务ID: {}, 已停止任务数: {}", parentTaskId, stoppedCount);
    }
}
