package com.dipper.monitor.mapper;


import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadCharReq;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: your name
 * @date: 2025-05-22
 * @description: ES线程池监控指标 Mapper
 */
public interface ElasticThreadMetricMapper {

    /**
     * 插入一条线程池监控数据
     */
    void insert(ThreadMetricEntity metric);

    /**
     * 批量插入线程池监控数据
     */
    void batchInsert(@Param("list") List<ThreadMetricEntity> list);

    /**
     * 根据主键删除记录（可选）
     */
    void deleteById(Long id);

    /**
     * 根据时间范围删除旧数据（用于清理历史数据）
     */
    Integer deleteByCollectTimeBefore(LocalDateTime beforeTime);

    /**
     * 查询所有记录（谨慎使用）
     */
    List<ThreadMetricEntity> selectAll();

    /**
     * 根据集群编码、节点名和线程类型查询指定时间段内的监控数据
     */
    List<ThreadMetricEntity> selectByClusterNodeAndType(
            @Param("clusterCode") String clusterCode,
            @Param("nodeName") String nodeName,
            @Param("threadType") String threadType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询某个节点在一段时间内所有类型的线程数据
     */
    List<ThreadMetricEntity> selectByClusterNodeAndTime(
            @Param("clusterCode") String clusterCode,
            @Param("nodeName") String nodeName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询某个集群下所有节点的某类线程池数据
     */
    List<ThreadMetricEntity> selectByClusterAndType(
            @Param("clusterCode") String clusterCode,
            @Param("threadType") String threadType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最新的一条记录（按采集时间倒序取第一条）
     */
    ThreadMetricEntity selectLatest();

    /**
     * 查询某个节点的最新一条记录
     */
    ThreadMetricEntity selectLatestByNode(
            @Param("clusterCode") String clusterCode,
            @Param("nodeName") String nodeName,
            @Param("threadType") String threadType);

    /**
     * 统计某个时间段内拒绝任务总数
     */
    Long countRejectedTasks(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<ThreadMetricEntity> getThreadMetrics(ThreadCharReq threadCharReq);
}