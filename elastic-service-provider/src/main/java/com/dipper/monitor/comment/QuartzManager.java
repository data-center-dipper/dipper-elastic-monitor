package com.dipper.monitor.comment;

import com.dipper.monitor.task.ITask;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QuartzManager {

    @Autowired
    private Scheduler scheduler;

    public void scheduleJob(ITask task) throws SchedulerException {
        String name = task.getTaskName();
        String group = task.getAuthor();

        JobDetail jobDetail = JobBuilder.newJob(DynamicTaskJob.class)
                .withIdentity(name, group)
                .usingJobData("taskName", name)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(name + "_trigger", group)
                .withSchedule(CronScheduleBuilder.cronSchedule(task.getCron()))
                .build();

        jobDetail.getJobDataMap().put("task", task);

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void updateCron(String taskName, String group, String newCron) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(taskName + "_trigger", group);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        if (trigger == null) return;

        Trigger updated = trigger.getTriggerBuilder()
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(newCron))
                .build();

        scheduler.rescheduleJob(triggerKey, updated);
    }

    public void pauseJob(String taskName, String group) throws SchedulerException {
        scheduler.pauseJob(JobKey.jobKey(taskName, group));
    }

    public void resumeJob(String taskName, String group) throws SchedulerException {
        scheduler.resumeJob(JobKey.jobKey(taskName, group));
    }

    public void triggerJobOnce(String taskName, String group) throws SchedulerException {
        scheduler.triggerJob(JobKey.jobKey(taskName, group));
    }

    public void deleteJob(String taskName, String group) throws SchedulerException {
        scheduler.deleteJob(JobKey.jobKey(taskName, group));
    }
}