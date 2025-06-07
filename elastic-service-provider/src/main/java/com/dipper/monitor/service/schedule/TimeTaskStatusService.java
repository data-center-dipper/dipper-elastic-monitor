package com.dipper.monitor.service.schedule;

import com.dipper.monitor.entity.task.TaskMetadataEntity;

import java.util.List;

public interface TimeTaskStatusService {

    /**
     * 停止任务
     *
     * @param taskId 任务ID
     */
    void taskPause(Integer taskId);

    /**
     * 停止任务
     *
     * @param taskId 任务ID
     */
    void taskStop(Integer taskId);

    /**
     * 启动任务
     *
     * @param taskId 任务ID
     */
    void taskStart(Integer taskId);

    /**
     * 执行一次任务
     *
     * @param taskId 任务ID
     */
    void taskExecute(Integer taskId);



}
