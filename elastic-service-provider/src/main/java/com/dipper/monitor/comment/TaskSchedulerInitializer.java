package com.dipper.monitor.comment;

import com.dipper.monitor.entity.task.TaskMetadataEntity;
import com.dipper.monitor.enums.elastic.TaskStatusEnum;
import com.dipper.monitor.service.schedule.TimeTaskStoreService;
import com.dipper.monitor.task.ITask;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class TaskSchedulerInitializer implements CommandLineRunner {

    @Autowired
    private List<ITask> tasks;

    @Autowired
    private QuartzManager quartzManager;
    @Autowired
    private TimeTaskStoreService timeTaskStoreService;

    @Override
    public void run(String... args) {
        List<TaskMetadataEntity> list = new ArrayList<>();
        for (ITask task : tasks) {
            try {
                TaskMetadataEntity taskMetadataEntity = transTo(task);
                list.add(taskMetadataEntity);

//                TaskMetadataEntity taskStatus = timeTaskStoreService.findTaskStatus(taskMetadataEntity);
//                if (taskStatus == null) {
//                    quartzManager.scheduleJob(task);
//                }else {
//                    String status = taskStatus.getStatus();
//                    if(TaskStatusEnum.STOPPED.getStatus().equals(status)) {
//                        log.info("任务 {} 已经停止，不能参与调度", task.getTaskName());
//                    }
//                }

                quartzManager.scheduleJob(task);

            } catch (SchedulerException e) {
                log.error("任务 {} 注册失败", task.getTaskName(), e);
            }

        }
        timeTaskStoreService.saveOrUpdateTask(list);
    }

    private TaskMetadataEntity transTo(ITask task) {
        TaskMetadataEntity entity = new TaskMetadataEntity();

        // 获取类信息
        Class<? extends ITask> aClass = task.getClass();
        entity.setClassName(aClass.getName());
        entity.setAnnotationType("com.dipper.monitor.annotation.quartz.QuartzJob");

        // 获取任务基本信息
        entity.setTaskName(task.getTaskName());
        entity.setAuthor(task.getAuthor());
        entity.setGroupName(task.getAuthor()); // 组名与作者一致，可根据需求调整
        entity.setJobDesc(task.getJobDesc());
        entity.setCron(task.getCron());
        entity.setEditAble(task.isEditable());

        // 固定速率/延迟为空（本方案基于 cron）
        entity.setFixedRate(null);
        entity.setFixedDelay(null);

        // 方法名（假设所有任务执行都是 execute 方法）
        entity.setMethodName("execute");

        // 其他属性（JSON格式），可选
        entity.setAdditionalAttributes("{}"); // 可根据需要序列化附加信息

        // 时间戳
        Date now = new Date();
        entity.setCreateTime(now);  // 若持久化后应从 DB 获取
        entity.setUpdateTime(now);  // 若有更新时间也可单独处理

        return entity;
    }

    public void taskExecute(String taskName, String groupName) {
        for (ITask task : tasks) {
            if (task.getTaskName().equals(taskName) && task.getAuthor().equals(groupName)) {
                log.info("执行任务: {}", task.getTaskName());
                task. execute();
            }
        }
    }
}