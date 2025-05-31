package com.dipper.monitor.service.elastic.nodes;

import com.dipper.monitor.entity.db.elastic.ElasticNodeMetricEntity;
import com.dipper.monitor.entity.elastic.nodes.metric.NodeMetricHistoryReq;
import com.dipper.monitor.entity.elastic.nodes.metric.NodeMetricHistoryView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 节点指标存储服务接口
 */
public interface NodeMetricStoreService {
    
    /**
     * 批量保存节点指标
     * @param metrics 节点指标列表
     * @return 保存成功的记录数
     */
    int batchSaveNodeMetrics(List<ElasticNodeMetricEntity> metrics);
    
    /**
     * 根据集群编码和节点名称查询最新的节点指标
     * @param clusterCode 集群编码
     * @param nodeName 节点名称
     * @return 节点指标实体
     */
    ElasticNodeMetricEntity getLatestNodeMetric(String clusterCode, String nodeName);
    
    /**
     * 根据集群编码查询所有节点的最新指标
     * @param clusterCode 集群编码
     * @return 节点指标列表
     */
    List<ElasticNodeMetricEntity> getLatestNodeMetrics(String clusterCode);
    
    /**
     * 根据集群编码和时间范围查询节点指标历史数据
     * @return 节点指标列表
     */
    NodeMetricHistoryView getNodeMetricHistory(NodeMetricHistoryReq nodeMetricHistoryReq);
    
    /**
     * 获取节点指标历史数据总数
     * @param clusterCode 集群编码
     * @param nodeName 节点名称，可为null，表示查询所有节点
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 记录总数
     */
    int getNodeMetricHistoryCount(String clusterCode, String nodeName, 
                                 LocalDateTime startTime, LocalDateTime endTime);

    List<ElasticNodeMetricEntity> selectHistoryByCondition(String clusterCode, String nodeName, Instant startTime, Instant endTime);
}
