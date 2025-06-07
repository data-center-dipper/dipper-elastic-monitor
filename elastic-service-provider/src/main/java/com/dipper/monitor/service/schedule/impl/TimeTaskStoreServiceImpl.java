package com.dipper.monitor.service.schedule.impl;

import com.dipper.monitor.entity.task.TaskMetadataEntity;
import com.dipper.monitor.entity.task.TaskListView;
import com.dipper.monitor.entity.task.TaskPageReq;
import com.dipper.monitor.enums.elastic.TaskStatusEnum;
import com.dipper.monitor.mapper.TaskMetadataMapper;
import com.dipper.monitor.service.schedule.MetadataStorage;
import com.dipper.monitor.service.schedule.QuartzJobService;
import com.dipper.monitor.service.schedule.TimeTaskStoreService;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class TimeTaskStoreServiceImpl implements TimeTaskStoreService {

    @Autowired
    private TaskMetadataMapper taskMetadataMapper;

    @Autowired
    @Qualifier("localMySQLStorage")
    private MetadataStorage localMySQLStorage;

    @Autowired
    @Qualifier("remoteRESTStorage")
    private MetadataStorage remoteRESTStorage;

    @Override
    public Tuple2<List<TaskListView>, Long> taskPage(TaskPageReq taskPageReq) {
        // 计算分页偏移量
        int offset = (taskPageReq.getPageNum() - 1) * taskPageReq.getPageSize();

        String status = taskPageReq.getStatus();
        // 查询总数
        Long total = taskMetadataMapper.countTasks(taskPageReq.getKeyword(),status);
        
        // 查询分页数据
        List<TaskMetadataEntity> entities = taskMetadataMapper.findTasksByPage(
                taskPageReq.getKeyword(), 
                taskPageReq.getPageSize(),
                status,
                offset);
        
        // 转换为视图对象
        List<TaskListView> taskViews = new ArrayList<>();
        for (TaskMetadataEntity entity : entities) {
            TaskListView view = new TaskListView();
            BeanUtils.copyProperties(entity, view);
            String statusDb = entity.getStatus();
            if(statusDb == null){
                view.setStatus(TaskStatusEnum.RUNNING.getStatus());
            }
            taskViews.add(view);
        }
        
        return new Tuple2<>(taskViews, total);
    }


    @Override
    public void saveOrUpdateTask(List<TaskMetadataEntity> metadataList) {

        if (metadataList == null || metadataList.isEmpty()) {
            log.warn("没有要存储的定时任务");
            return ;
        }

        String storageType = "local";
        // 根据存储类型选择存储实现
        MetadataStorage storage;
        if ("remote".equalsIgnoreCase(storageType)) {
            storage = remoteRESTStorage;
            log.info("使用远程REST存储");
        } else {
            storage = localMySQLStorage;
            log.info("使用本地MySQL存储");
        }

        // 保存元数据
        int count = storage.saveMetadata(metadataList);
    }

    @Override
    public TaskMetadataEntity findTaskStatus(TaskMetadataEntity taskMetadataEntity) {
        String author = taskMetadataEntity.getAuthor();
        String annotationType = taskMetadataEntity.getAnnotationType();
        String taskName = taskMetadataEntity.getTaskName();
        String groupName = taskMetadataEntity.getGroupName();

        TaskMetadataEntity byUnique = taskMetadataMapper.findByUnique(author, annotationType, taskName, groupName);
        return byUnique;
    }
}
