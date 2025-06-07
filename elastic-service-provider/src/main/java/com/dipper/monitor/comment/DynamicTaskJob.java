package com.dipper.monitor.comment;

import com.dipper.monitor.task.ITask;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class DynamicTaskJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        ITask task = (ITask) dataMap.get("task");
        if (task != null) {
            task.execute();
        }
    }
}