package com.dipper.monitor.service.schedule.impl;

import com.dipper.monitor.entity.task.TaskMetadataEntity;
import com.dipper.monitor.service.schedule.QuartzJobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class QuartzJobServiceImpl implements QuartzJobService {

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Override
    public void taskStop(TaskMetadataEntity task) throws SchedulerException {
        String methodName = task.getMethodName();
        String groupName = task.getGroupName();
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = new JobKey(methodName, groupName);
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey); // 暂停job
            // 如果需要彻底删除job，可以使用如下代码：
            // scheduler.deleteJob(jobKey);
            log.info("Job " + methodName + " in group " + groupName + " has been paused.");
        } else {
            log.error("Job " + methodName + " in group " + groupName + " does not exist.");
        }
    }

    @Override
    public void taskStart(TaskMetadataEntity task) throws ClassNotFoundException, SchedulerException {
        String methodName = task.getMethodName();
        String groupName = task.getGroupName();
        String className = task.getClassName();
        String cron = task.getCron();
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = new JobKey(methodName, groupName);
        if (!scheduler.checkExists(jobKey)) {
            // 如果不存在，则需要先添加JobDetail和Trigger
            Class<?> aClass = Class.forName(className);
            JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) aClass)
                    .withIdentity(jobKey)
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(methodName + "Trigger", groupName)
                    .startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron)) // 根据实际需求调整cron表达式
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
            System.out.println("Job " + methodName + " in group " + groupName + " has been started.");
        } else {
            // 如果任务存在但处于暂停状态，则恢复它
            scheduler.resumeJob(jobKey);
            System.out.println("Job " + methodName + " in group " + groupName + " has been resumed.");
        }
    }

    @Override
    public void taskExecute(TaskMetadataEntity task) {
       throw new IllegalArgumentException("Quartz does not support executing a job once.");
    }

}
