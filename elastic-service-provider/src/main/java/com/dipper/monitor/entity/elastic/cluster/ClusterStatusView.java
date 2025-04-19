package com.dipper.monitor.entity.elastic.cluster;

import lombok.Data;

/**
 * 集群状态响应实体类
 */
@Data
public class ClusterStatusView {
    private String epoch;
    private String timestamp;
    private String cluster;
    private String status;
    private int nodeTotal;
    private int nodeData;
    private String shards;
    private String pri;
    private String relo;
    private String init;
    private String unassign;
    private String pendingTasks;
    private String maxTaskWaitTime;
    private String activeShardsPercent;

    private ClusterStatsParse clusterStatus;
}