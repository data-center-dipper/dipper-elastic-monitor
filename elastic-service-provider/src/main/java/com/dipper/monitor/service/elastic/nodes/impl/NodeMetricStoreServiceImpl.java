package com.dipper.monitor.service.elastic.nodes.impl;

import com.dipper.monitor.entity.db.elastic.ElasticNodeMetricEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.nodes.metric.NodeMetricHistoryReq;
import com.dipper.monitor.entity.elastic.nodes.metric.NodeMetricHistoryView;
import com.dipper.monitor.mapper.NodeMetricStoreMapper;
import com.dipper.monitor.service.elastic.nodes.NodeMetricStoreService;
import com.dipper.monitor.service.elastic.nodes.impl.handlers.charts.NodeCharHistoryHandler;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class NodeMetricStoreServiceImpl implements NodeMetricStoreService {

    @Autowired
    private NodeMetricStoreMapper nodeMetricStoreMapper;
    
    @Override
    public int batchSaveNodeMetrics(List<ElasticNodeMetricEntity> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return 0;
        }
        try {
            return nodeMetricStoreMapper.batchInsert(metrics);
        } catch (Exception e) {
            log.error("批量保存节点指标失败", e);
            return 0;
        }
    }
    
    @Override
    public ElasticNodeMetricEntity getLatestNodeMetric(String clusterCode, String nodeName) {
        return nodeMetricStoreMapper.selectLatestByClusterAndNode(clusterCode, nodeName);
    }
    
    @Override
    public List<ElasticNodeMetricEntity> getLatestNodeMetrics(String clusterCode) {
        return nodeMetricStoreMapper.selectLatestByCluster(clusterCode);
    }

    @Override
    public NodeMetricHistoryView getNodeMetricHistory(NodeMetricHistoryReq nodeMetricHistoryReq) {
        NodeCharHistoryHandler nodeCharHistoryHandler = new NodeCharHistoryHandler(this);
        return nodeCharHistoryHandler.getNodeMetricHistory(nodeMetricHistoryReq);

    }
    
    @Override
    public int getNodeMetricHistoryCount(String clusterCode, String nodeName, 
                                        LocalDateTime startTime, LocalDateTime endTime) {
        return nodeMetricStoreMapper.countHistoryByCondition(clusterCode, nodeName, startTime, endTime);
    }

    @Override
    public List<ElasticNodeMetricEntity> selectHistoryByCondition(String clusterCode, String nodeId, String nodeName, Instant startTime, Instant endTime) {
        return nodeMetricStoreMapper.selectHistoryByCondition(clusterCode, nodeName, startTime, endTime);
    }
}
