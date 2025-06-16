package com.dipper.monitor.service.elastic.data.impl;

import com.dipper.monitor.entity.db.elastic.SunTaskEntity;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskView;
import com.dipper.monitor.mapper.MigrationSunTaskMapper;
import com.dipper.monitor.service.elastic.data.SunTaskExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class SunTaskExecutorServiceImpl implements SunTaskExecutorService {

    @Autowired
    private MigrationSunTaskMapper migrationSunTaskMapper;

    /**
     * 执行所有子任务
     * @param tasks 排序后的子任务列表
     * @param parentTask 父任务信息
     * @param concurrencyLimit 并发限制
     */
    public void executeAllTasks(List<SunTaskEntity> tasks, MigrationTaskView parentTask, Integer concurrencyLimit) {
        log.info("开始执行所有子任务，父任务ID: {}, 子任务数量: {}, 并发限制: {}", 
                parentTask.getTaskId(), tasks.size(), concurrencyLimit);
        
        // 创建固定大小的线程池，大小为并发限制数
        ExecutorService executorService = Executors.newFixedThreadPool(concurrencyLimit);
        
        try {
            // 使用CompletableFuture异步执行所有任务
            List<CompletableFuture<Void>> futures = tasks.stream()
                    .map(task -> CompletableFuture.runAsync(() -> {
                        try {
                            executeTask(task);
                        } catch (Exception e) {
                            log.error("子任务执行失败, 任务ID: {}, 索引: {}, 错误: {}", 
                                    task.getId(), task.getIndexName(), e.getMessage(), e);
                            // 更新任务状态为失败
                            updateTaskStatusToFailed(task, e.getMessage());
                        }
                    }, executorService))
                    .toList();
            
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            log.info("所有子任务执行完成，父任务ID: {}", parentTask.getTaskId());
        } finally {
            // 关闭线程池
            executorService.shutdown();
        }
    }
    
    /**
     * 执行单个子任务
     * @param task 子任务实体
     */
    public void executeTask(SunTaskEntity task) {
        log.info("开始执行子任务, 任务ID: {}, 索引: {}", task.getId(), task.getIndexName());
        
        try {
            // 更新任务状态为运行中
            updateTaskStatusToRunning(task);
            
            // TODO: 实现实际的任务执行逻辑
            // 这里应该根据实际业务需求实现数据迁移逻辑
            // 例如：从源集群读取数据，写入目标集群等
            
            // 模拟任务执行
            log.info("正在执行子任务, 任务ID: {}, 索引: {}", task.getId(), task.getIndexName());
            // 可以根据需要添加实际的执行逻辑
            
            // 更新任务状态为成功
            updateTaskStatusToSuccess(task);
            log.info("子任务执行成功, 任务ID: {}, 索引: {}", task.getId(), task.getIndexName());
        } catch (Exception e) {
            log.error("子任务执行异常, 任务ID: {}, 索引: {}, 错误: {}", 
                    task.getId(), task.getIndexName(), e.getMessage(), e);
            // 更新任务状态为失败
            updateTaskStatusToFailed(task, e.getMessage());
            throw e; // 重新抛出异常，让调用者知道任务失败
        }
    }
    
    /**
     * 更新任务状态为运行中
     */
    private void updateTaskStatusToRunning(SunTaskEntity task) {
        task.setStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        migrationSunTaskMapper.updateSubtaskStatus(task.getId(), task.getStatus());
        log.info("子任务状态已更新为RUNNING, 任务ID: {}", task.getId());
    }
    
    /**
     * 更新任务状态为成功
     */
    private void updateTaskStatusToSuccess(SunTaskEntity task) {
        task.setStatus("SUCCESS");
        task.setEndTime(LocalDateTime.now());
        migrationSunTaskMapper.updateSubtaskStatus(task.getId(), task.getStatus());
        log.info("子任务状态已更新为SUCCESS, 任务ID: {}", task.getId());
    }
    
    /**
     * 更新任务状态为失败
     */
    private void updateTaskStatusToFailed(SunTaskEntity task, String errorMessage) {
        task.setStatus("FAILED");
        task.setEndTime(LocalDateTime.now());
        task.setErrorLog(errorMessage);
        task.setRetryCount(task.getRetryCount() + 1);
        migrationSunTaskMapper.updateSubtaskStatus(task.getId(), task.getStatus());
        migrationSunTaskMapper.updateSubtaskErrorInfo(task.getId(), task.getErrorLog(), task.getRetryCount());
        log.info("子任务状态已更新为FAILED, 任务ID: {}", task.getId());
    }
}
