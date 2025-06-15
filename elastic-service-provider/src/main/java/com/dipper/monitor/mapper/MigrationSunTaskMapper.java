package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.db.elastic.SunTaskEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MigrationSunTaskMapper {

    void insertSubtask(SunTaskEntity subtask);

    List<SunTaskEntity> selectSubtasksByPage(@Param("pageNum") int pageNum,
                                             @Param("pageSize") int pageSize);

    int countAllSubtasks();

    SunTaskEntity selectSubtaskById(Long id);

    int updateSubtaskStatus(@Param("id") Long id,
                            @Param("status") String status);

    int updateSubtaskErrorInfo(@Param("id") Long id, @Param("errorLog") String errorLog,
                               @Param("retryCount") int retryCount);
}