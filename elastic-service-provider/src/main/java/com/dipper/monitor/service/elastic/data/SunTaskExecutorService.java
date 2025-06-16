package com.dipper.monitor.service.elastic.data;

import com.dipper.monitor.entity.db.elastic.SunTaskEntity;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskView;

import java.util.List;

public interface SunTaskExecutorService {
    /**
     * 执行所有子任务
     * @param tasks 排序后的子任务列表
     * @param parentTask 父任务信息
     * @param concurrencyLimit 并发限制
     */
    void executeAllTasks(List<SunTaskEntity> tasks, MigrationTaskView parentTask, Integer concurrencyLimit);
    
    /**
     * 执行单个子任务
     * @param task 子任务实体
     */
    void executeTask(SunTaskEntity task);
}
