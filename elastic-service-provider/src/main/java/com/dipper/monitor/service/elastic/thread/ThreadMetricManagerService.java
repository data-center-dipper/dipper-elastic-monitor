package com.dipper.monitor.service.elastic.thread;

import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: hydra
 * @date: 2023-05-22
 * @description: ES线程池指标管理服务接口
 */
public interface ThreadMetricManagerService {
    
    /**
     * 保存线程池指标数据
     * @param metrics 线程池指标列表
     */
    void saveThreadMetrics(List<ThreadMetricEntity> metrics);
    
    /**
     * 清理历史数据
     * @param retentionDays 保留天数
     */
    void cleanHistoryData(int retentionDays);
    
    /**
     * 根据集群、节点和线程类型查询指标
     * @param clusterCode 集群编码
     * @param nodeName 节点名称
     * @param threadType 线程类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 线程池指标列表
     */
    List<ThreadMetricEntity> getThreadMetricsByClusterAndNode(String clusterCode, String nodeName,
                                                              String threadType, LocalDateTime startTime, 
                                                              LocalDateTime endTime);
    
    /**
     * 根据集群和线程类型查询指标
     * @param clusterCode 集群编码
     * @param threadType 线程类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 线程池指标列表
     */
    List<ThreadMetricEntity> getThreadMetricsByClusterAndType(String clusterCode, String threadType,
                                                              LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取最新的线程池指标
     * @param clusterCode 集群编码
     * @param nodeName 节点名称
     * @param threadType 线程类型
     * @return 最新的线程池指标
     */
    ThreadMetricEntity getLatestThreadMetric(String clusterCode, String nodeName, String threadType);
}