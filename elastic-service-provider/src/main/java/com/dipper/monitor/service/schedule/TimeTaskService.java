package com.dipper.monitor.service.schedule;

import com.dipper.monitor.entity.task.TaskListView;
import com.dipper.monitor.entity.task.TaskPageReq;
import com.dipper.monitor.utils.Tuple2;

import java.util.List;

/**
 * 定时任务服务接口
 */
public interface TimeTaskService {
    
    /**
     * 分页查询任务
     * 
     * @param taskPageReq 分页查询请求
     * @return 任务列表和总数
     */
    Tuple2<List<TaskListView>, Long> taskPage(TaskPageReq taskPageReq);
    
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
