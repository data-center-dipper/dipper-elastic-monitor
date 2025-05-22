package com.dipper.monitor.service.elastic.thread.impl;

import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.mapper.ElasticThreadMetricMapper;
import com.dipper.monitor.service.elastic.thread.ThreadMetricManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: hydra
 * @date: 2023-05-22
 * @description: ES线程池指标管理服务实现
 */
@Slf4j
@Service
public class ThreadMetricManagerServiceImpl implements ThreadMetricManagerService {

    @Autowired
    private ElasticThreadMetricMapper elasticThreadMetricMapper;

    @Override
    public void saveThreadMetrics(List<ThreadMetricEntity> metrics) {
        try {
            if (metrics == null || metrics.isEmpty()) {
                log.warn("没有线程池指标数据需要保存");
                return;
            }
            
            elasticThreadMetricMapper.batchInsert(metrics);
            log.info("成功保存 {} 条线程池指标数据", metrics.size());
        } catch (Exception e) {
            log.error("保存线程池指标数据失败", e);
            throw e;
        }
    }

    @Override
    public void cleanHistoryData(int retentionDays) {
        try {
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(retentionDays);
            int deletedCount = elasticThreadMetricMapper.deleteByCollectTimeBefore(beforeTime);
            log.info("成功清理 {} 条历史线程池指标数据（保留 {} 天）", deletedCount, retentionDays);
        } catch (Exception e) {
            log.error("清理历史线程池指标数据失败", e);
        }
    }

    @Override
    public List<ThreadMetricEntity> getThreadMetricsByClusterAndNode(String clusterCode, String nodeName,
                                                                     String threadType, LocalDateTime startTime, 
                                                                     LocalDateTime endTime) {
        return elasticThreadMetricMapper.selectByClusterNodeAndType(clusterCode, nodeName, threadType, startTime, endTime);
    }

    @Override
    public List<ThreadMetricEntity> getThreadMetricsByClusterAndType(String clusterCode, String threadType,
                                                                     LocalDateTime startTime, LocalDateTime endTime) {
        return elasticThreadMetricMapper.selectByClusterAndType(clusterCode, threadType, startTime, endTime);
    }

    @Override
    public ThreadMetricEntity getLatestThreadMetric(String clusterCode, String nodeName, String threadType) {
        return elasticThreadMetricMapper.selectLatestByNode(clusterCode, nodeName, threadType);
    }
}