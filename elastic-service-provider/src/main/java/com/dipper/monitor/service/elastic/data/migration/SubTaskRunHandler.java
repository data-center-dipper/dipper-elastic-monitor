package com.dipper.monitor.service.elastic.data.migration;


import com.dipper.monitor.entity.elastic.data.migration.sun.SunRunTaskReq;
import com.dipper.monitor.service.elastic.data.MigrationSunTaskService;
import com.dipper.monitor.service.elastic.data.impl.MigrationSunTaskServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SubTaskRunHandler  extends AbstractSubTaskRunHandler {

    public SubTaskRunHandler(MigrationSunTaskService migrationSunTaskService) {
        super(migrationSunTaskService);
    }

    public void runOneSubtask(SunRunTaskReq sunRunTaskReq) {
    }
}
