package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.db.elastic.ElasticNodeMetricEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface NodeMetricStoreMapper {

    public int batchInsert(List<ElasticNodeMetricEntity> metrics) ;

    public ElasticNodeMetricEntity selectLatestByClusterAndNode(String clusterCode, String nodeName) ;

    public List<ElasticNodeMetricEntity> selectLatestByCluster(String clusterCode)  ;

    public List<ElasticNodeMetricEntity> selectHistoryByCondition(String clusterCode,
                                                                  String nodeName,
                                                                  Instant startTime, Instant endTime) ;

    public int countHistoryByCondition(String clusterCode, String nodeName,
                                       LocalDateTime startTime, LocalDateTime endTime)  ;
}
