package com.dipper.monitor.service.elastic.data.migration;

import com.dipper.monitor.aware.SpringBeanAwareUtils;
import com.dipper.monitor.service.elastic.data.MigrationParentTaskService;
import com.dipper.monitor.service.elastic.data.MigrationSunTaskService;
import com.dipper.monitor.service.elastic.data.SunTaskExecutorService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractSubTaskRunHandler {

    protected MigrationSunTaskService migrationSunTaskService;
    protected MigrationParentTaskService migrationParentTaskService ;
    protected SunTaskExecutorService sunTaskExecutorService ;

    public AbstractSubTaskRunHandler(MigrationSunTaskService migrationSunTaskService,
                                     SunTaskExecutorService sunTaskExecutorService) {
        this.migrationSunTaskService = migrationSunTaskService;
        this.migrationParentTaskService = SpringBeanAwareUtils.getBean(MigrationParentTaskService.class);
        this.sunTaskExecutorService = sunTaskExecutorService;
    }
}
