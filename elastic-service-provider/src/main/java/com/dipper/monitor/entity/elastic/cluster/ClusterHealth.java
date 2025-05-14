package com.dipper.monitor.entity.elastic.cluster;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonSetter;

@Data
public class ClusterHealth {
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
    private String unassignPri;
    private String pendingTasks;
    private String maxTaskWaitTime;
    private String activeShardsPercent;

}