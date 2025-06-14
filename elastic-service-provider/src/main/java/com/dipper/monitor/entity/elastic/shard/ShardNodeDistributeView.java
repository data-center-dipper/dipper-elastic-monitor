package com.dipper.monitor.entity.elastic.shard;

import lombok.Data;

import java.util.List;

@Data
public class ShardNodeDistributeView {

    // 节点数
    private String nodeNum;
    // 分片总数
    private String shardNum;
    // 主分片数
    private String primaryShardNum;
    // 副本分片数
    private String replicaShardNum;
    // 集群分片每个节点的限制
    private Integer shardLimit;
    // 当前总分片数
    private Integer currentShards;

    // 节点角度的shard分布情况
    private List<ShardNodeView> shardNodeViews;
}
