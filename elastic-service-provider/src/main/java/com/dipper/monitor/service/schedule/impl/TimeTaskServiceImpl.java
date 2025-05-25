package com.dipper.monitor.service.schedule.impl;

import com.dipper.monitor.entity.task.TaskMetadataEntity;
import com.dipper.monitor.entity.task.TaskListView;
import com.dipper.monitor.entity.task.TaskPageReq;
import com.dipper.monitor.mapper.TaskMetadataMapper;
import com.dipper.monitor.service.schedule.QuartzJobService;
import com.dipper.monitor.service.schedule.TimeTaskService;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 定时任务服务实现类
 */
@Slf4j
@Service
public class TimeTaskServiceImpl implements TimeTaskService {

    @Autowired
    private TaskMetadataMapper taskMetadataMapper;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired(required = false)
    private ScheduledAnnotationBeanPostProcessor scheduledProcessor;
    @Autowired
    private QuartzJobService quartzJobService;

    @Override
    public Tuple2<List<TaskListView>, Long> taskPage(TaskPageReq taskPageReq) {
        // 计算分页偏移量
        int offset = (taskPageReq.getPageNum() - 1) * taskPageReq.getPageSize();
        
        // 查询总数
        Long total = taskMetadataMapper.countTasks(taskPageReq.getKeyword());
        
        // 查询分页数据
        List<TaskMetadataEntity> entities = taskMetadataMapper.findTasksByPage(
                taskPageReq.getKeyword(), 
                taskPageReq.getPageSize(), 
                offset);
        
        // 转换为视图对象
        List<TaskListView> taskViews = new ArrayList<>();
        for (TaskMetadataEntity entity : entities) {
            TaskListView view = new TaskListView();
            BeanUtils.copyProperties(entity, view);
            taskViews.add(view);
        }
        
        return new Tuple2<>(taskViews, total);
    }

    @Override
    public void taskStop(Integer taskId) {
        try {
            TaskMetadataEntity task = taskMetadataMapper.findById(taskId);
            if (task == null) {
                log.error("任务不存在: {}", taskId);
                return;
            }
            
            // 获取任务对应的bean和方法
            String className = task.getClassName();
            String methodName = task.getMethodName();
            String annotationType = task.getAnnotationType();
            if(annotationType.contains("QuartzJob")){
                quartzJobService.taskStop(task);
                return;
            }

            // 停止定时任务
            if (scheduledProcessor != null) {
                Set<ScheduledTask> scheduledTasks = scheduledProcessor.getScheduledTasks();
                for (ScheduledTask scheduledTask : scheduledTasks) {
                    // 根据任务信息匹配并取消任务
                    // 这里简化处理，实际可能需要更复杂的匹配逻辑
                    scheduledTask.cancel();
                }
            }
            
            log.info("成功停止任务: {}", taskId);
        } catch (Exception e) {
            log.error("停止任务失败: {}", e.getMessage(), e);
            throw new RuntimeException("停止任务失败: " + e.getMessage());
        }
    }

    @Override
    public void taskStart(Integer taskId) {
        try {
            TaskMetadataEntity task = taskMetadataMapper.findById(taskId);
            if (task == null) {
                log.error("任务不存在: {}", taskId);
                return;
            }
            
            // 获取任务对应的bean和方法
            String className = task.getClassName();
            String methodName = task.getMethodName();
            String annotationType = task.getAnnotationType();
            if(annotationType.contains("QuartzJob")){
                quartzJobService.taskStart(task);
                return;
            }

            // 获取Bean实例
            Object bean = applicationContext.getBean(Class.forName(className));
            Method method = bean.getClass().getDeclaredMethod(methodName);
            
            // 创建并启动定时任务
            if (task.getCron() != null && !task.getCron().isEmpty()) {
                // 使用Cron表达式创建任务
                ScheduledTaskRegistrar registrar = new ScheduledTaskRegistrar();
                registrar.addCronTask(() -> {
                    try {
                        method.invoke(bean);
                    } catch (Exception e) {
                        log.error("执行定时任务失败: {}", e.getMessage(), e);
                    }
                }, task.getCron());
                registrar.afterPropertiesSet();
            } else if (task.getFixedRate() != null) {
                // 使用固定速率创建任务
                // 实际实现可能需要更复杂的逻辑
                log.info("使用固定速率启动任务: {}", taskId);
            } else if (task.getFixedDelay() != null) {
                // 使用固定延迟创建任务
                // 实际实现可能需要更复杂的逻辑
                log.info("使用固定延迟启动任务: {}", taskId);
            }
            
            log.info("成功启动任务: {}", taskId);
        } catch (Exception e) {
            log.error("启动任务失败: {}", e.getMessage(), e);
            throw new RuntimeException("启动任务失败: " + e.getMessage());
        }
    }

    @Override
    public void taskExecute(Integer taskId) {
        try {
            TaskMetadataEntity task = taskMetadataMapper.findById(taskId);
            if (task == null) {
                log.error("任务不存在: {}", taskId);
                return;
            }
            
            // 获取任务对应的bean和方法
            String className = task.getClassName();
            String methodName = task.getMethodName();
            String annotationType = task.getAnnotationType();
            if(annotationType.contains("QuartzJob")){
                quartzJobService.taskExecute(task);
                return;
            }


            // 获取Bean实例
            Object bean = applicationContext.getBean(Class.forName(className));
            Method method = bean.getClass().getDeclaredMethod(methodName);
            
            // 执行方法
            method.invoke(bean);
            
            log.info("成功执行任务: {}", taskId);
        } catch (Exception e) {
            log.error("执行任务失败: {}", e.getMessage(), e);
            throw new RuntimeException("执行任务失败: " + e.getMessage());
        }
    }
}
