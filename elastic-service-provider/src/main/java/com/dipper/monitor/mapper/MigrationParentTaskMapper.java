package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskReq;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskView;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MigrationParentTaskMapper {

    void insertTask(MigrationTaskReq task);

    void updateTask(MigrationTaskReq task);

    int deleteTaskById(Long id);

    MigrationTaskView selectTaskById(Long id);

    List<MigrationTaskView> selectTasksByPage(@Param("offset") int offset, @Param("limit") int limit);

    int countAllTasks();

    MigrationTaskView getOneTaskByTaskId(@Param("taskId") String taskId);
}