package com.dipper.monitor.service.elastic.data;

import com.dipper.monitor.entity.db.elastic.SunTaskEntity;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.data.migration.sun.SunRunTaskReq;
import com.dipper.monitor.entity.elastic.data.migration.sun.SunTaskView;
import com.dipper.monitor.utils.Tuple2;

import java.util.List;

public interface MigrationSunTaskService {

    Tuple2<List<SunTaskView>, Long> getSunTaskByPage(PageReq pageReq);

    void insertTask(List<SunTaskEntity> subTask);

    void startAllSubtasks(SunRunTaskReq sunRunTaskReq);

    void stopAllSubtasks(SunRunTaskReq sunRunTaskReq);

    void retryFailedSubtasks(SunRunTaskReq sunRunTaskReq);

    void runOneSubtask(SunRunTaskReq sunRunTaskReq);

    List<SunTaskEntity> getSunTaskByParentTaskId(String parentTaskId);

    void updateTask(SunTaskEntity task);

    SunTaskEntity getSunTaskById(Long aLong);
}
