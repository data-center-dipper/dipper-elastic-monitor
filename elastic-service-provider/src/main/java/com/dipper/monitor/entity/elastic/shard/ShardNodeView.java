package com.dipper.monitor.entity.elastic.shard;

import lombok.Data;

@Data
public class ShardNodeView {
    // 节点名称
    private String nodeName;
    // 节点IP
    private String nodeIp;
    // 分片总数
    private String shardNum;
    // 主分片数
    private String primaryShardNum;
    // 副本分片数
    private String replicaShardNum;
    // 节点磁盘使用率
    private double diskUsage;
    // 节点内存使用率
    private double cpuUsage;
}
