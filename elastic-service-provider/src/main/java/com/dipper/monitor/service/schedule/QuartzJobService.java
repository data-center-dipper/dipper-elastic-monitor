package com.dipper.monitor.service.schedule;

import com.dipper.monitor.entity.task.TaskMetadataEntity;
import org.quartz.SchedulerException;

public interface QuartzJobService {
    void taskStop(TaskMetadataEntity task) throws SchedulerException;

    void taskStart(TaskMetadataEntity task) throws ClassNotFoundException, SchedulerException;

    void taskExecute(TaskMetadataEntity task);
}
