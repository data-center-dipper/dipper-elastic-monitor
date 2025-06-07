package com.dipper.monitor.service.schedule.impl;

import com.dipper.monitor.aware.SpringBeanAwareUtils;
import com.dipper.monitor.comment.QuartzManager;
import com.dipper.monitor.comment.TaskSchedulerInitializer;
import com.dipper.monitor.entity.task.TaskMetadataEntity;
import com.dipper.monitor.service.schedule.QuartzJobService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class QuartzJobServiceImpl implements QuartzJobService {

    @Autowired
    private QuartzManager quartzManager;


    @Override
    public void taskStop(TaskMetadataEntity task) throws SchedulerException {
        String methodName = task.getMethodName();
        String taskName = task.getTaskName();
        String author = task.getAuthor();
        String groupName = task.getGroupName();
        if (StringUtils.isBlank(groupName)){
            groupName = author;
        }

        quartzManager.pauseJob(taskName,groupName);
    }

    @Override
    public void taskStart(TaskMetadataEntity task) throws ClassNotFoundException, SchedulerException {
        String methodName = task.getMethodName();
        String taskName = task.getTaskName();
        String author = task.getAuthor();
        String groupName = task.getGroupName();
        if (StringUtils.isBlank(groupName)){
            groupName = author;
        }

        quartzManager.resumeJob(taskName,groupName);
    }

    @Override
    public void taskExecute(TaskMetadataEntity task) {
        String methodName = task.getMethodName();
        String taskName = task.getTaskName();
        String author = task.getAuthor();
        String groupName = task.getGroupName();
        if (StringUtils.isBlank(groupName)){
            groupName = author;
        }
        if(StringUtils.isBlank(taskName)){
            throw new IllegalArgumentException("任务名称不能为空");
        }
        if(StringUtils.isBlank(groupName)){
            throw new IllegalArgumentException("任务group不能为空");
        }

        TaskSchedulerInitializer taskSchedulerInitializer = SpringBeanAwareUtils.getBean(TaskSchedulerInitializer.class);
        taskSchedulerInitializer.taskExecute(taskName,groupName);
    }

    @Override
    public void taskPause(TaskMetadataEntity task) throws SchedulerException {
        String methodName = task.getMethodName();
        String taskName = task.getTaskName();
        String author = task.getAuthor();
        String groupName = task.getGroupName();
        if (StringUtils.isBlank(groupName)){
            groupName = author;
        }

        quartzManager.pauseJob(taskName,groupName);
    }

}
