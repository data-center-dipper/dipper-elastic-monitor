package com.dipper.monitor.service.elastic.data.migration;

import com.dipper.monitor.entity.db.elastic.SunTaskEntity;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskView;
import com.dipper.monitor.entity.elastic.data.migration.sun.SunRunTaskReq;
import com.dipper.monitor.service.elastic.data.MigrationSunTaskService;
import com.dipper.monitor.service.elastic.data.SunTaskExecutorService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SubTaskAllRunHandler extends AbstractSubTaskRunHandler {

    public SubTaskAllRunHandler(MigrationSunTaskService migrationSunTaskService, SunTaskExecutorService sunTaskExecutorService) {
        super(migrationSunTaskService,sunTaskExecutorService);
    }

    public void startAllSubtasks(SunRunTaskReq sunRunTaskReq) {
        String parentTaskId = sunRunTaskReq.getParentTaskId();

        MigrationTaskView oneTaskByTaskId = migrationParentTaskService.getOneTaskByTaskId(parentTaskId);
        if (oneTaskByTaskId == null) {
            throw new RuntimeException("未找到对应的任务");
        }
        List<SunTaskEntity> sunTaskByParentTaskId = migrationSunTaskService.getSunTaskByParentTaskId(parentTaskId);
        if (sunTaskByParentTaskId == null || sunTaskByParentTaskId.size() == 0) {
            throw new RuntimeException("未找到对应的子任务");
        }

    }
}
