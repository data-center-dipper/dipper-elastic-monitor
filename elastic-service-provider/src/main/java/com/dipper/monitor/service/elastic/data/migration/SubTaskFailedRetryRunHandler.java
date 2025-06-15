package com.dipper.monitor.service.elastic.data.migration;

import com.dipper.monitor.entity.elastic.data.migration.sun.SunRunTaskReq;
import com.dipper.monitor.service.elastic.data.MigrationSunTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SubTaskFailedRetryRunHandler  extends AbstractSubTaskRunHandler {

    public SubTaskFailedRetryRunHandler(MigrationSunTaskService migrationSunTaskService) {
        super(migrationSunTaskService);
    }

    public void retryFailedSubtasks(SunRunTaskReq sunRunTaskReq) {

    }
}
