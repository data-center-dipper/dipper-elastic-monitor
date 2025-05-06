package com.dipper.monitor.entity.elastic.cluster;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonSetter;

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
    private String pendingTasks;
    private String maxTaskWaitTime;
    private String activeShardsPercent;

    // Getter 和 Setter 方法

    public String getEpoch() {
        return epoch;
    }

    public void setEpoch(String epoch) {
        this.epoch = epoch;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonSetter("node.total")
    public void setNodeTotal(String nodeTotal) {
        this.nodeTotal = Integer.parseInt(nodeTotal);
    }

    public int getNodeTotal() {
        return nodeTotal;
    }

    @JsonSetter("node.data")
    public void setNodeData(String nodeData) {
        this.nodeData = Integer.parseInt(nodeData);
    }

    public int getNodeData() {
        return nodeData;
    }

    public String getShards() {
        return shards;
    }

    public void setShards(String shards) {
        this.shards = shards;
    }

    public String getPri() {
        return pri;
    }

    public void setPri(String pri) {
        this.pri = pri;
    }

    public String getRelo() {
        return relo;
    }

    public void setRelo(String relo) {
        this.relo = relo;
    }

    public String getInit() {
        return init;
    }

    public void setInit(String init) {
        this.init = init;
    }

    public String getUnassign() {
        return unassign;
    }

    public void setUnassign(String unassign) {
        this.unassign = unassign;
    }

    @JsonSetter("pending_tasks")
    public void setPendingTasks(String pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public String getPendingTasks() {
        return pendingTasks;
    }

    @JsonSetter("max_task_wait_time")
    public void setMaxTaskWaitTime(String maxTaskWaitTime) {
        this.maxTaskWaitTime = maxTaskWaitTime;
    }

    public String getMaxTaskWaitTime() {
        return maxTaskWaitTime;
    }

    @JsonSetter("active_shards_percent")
    public void setActiveShardsPercent(String activeShardsPercent) {
        this.activeShardsPercent = activeShardsPercent;
    }

    public String getActiveShardsPercent() {
        return activeShardsPercent;
    }
}