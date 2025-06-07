package com.dipper.monitor.service.schedule;

import com.dipper.monitor.entity.task.TaskListView;
import com.dipper.monitor.entity.task.TaskMetadataEntity;
import com.dipper.monitor.entity.task.TaskPageReq;
import com.dipper.monitor.utils.Tuple2;

import java.util.List;

/**
 * 定时任务服务接口
 */
public interface TimeTaskStoreService {
    
    /**
     * 分页查询任务
     * 
     * @param taskPageReq 分页查询请求
     * @return 任务列表和总数
     */
    Tuple2<List<TaskListView>, Long> taskPage(TaskPageReq taskPageReq);

    /**
     * 存储任务信息
     * @param list
     */
    void saveOrUpdateTask(List<TaskMetadataEntity> list);

    TaskMetadataEntity findTaskStatus(TaskMetadataEntity taskMetadataEntity);
}
