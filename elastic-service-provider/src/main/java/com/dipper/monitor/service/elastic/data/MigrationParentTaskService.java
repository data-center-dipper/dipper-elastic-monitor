package com.dipper.monitor.service.elastic.data;

import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskReq;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskView;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.util.List;

public interface MigrationParentTaskService {
    Tuple2<List<MigrationTaskView>, Long> getPoliciesByPage(PageReq pageReq);

    void addTask(MigrationTaskReq migrationTaskReq) throws IOException;

    void updateTask(MigrationTaskReq migrationTaskReq);

    void deleteTask(Integer id);

    MigrationTaskView getOneTask(Integer id);

    MigrationTaskView getOneTaskByTaskId(String parentTaskId);
}
