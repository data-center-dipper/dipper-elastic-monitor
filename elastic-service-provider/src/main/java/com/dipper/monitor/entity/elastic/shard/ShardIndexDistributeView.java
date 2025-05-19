package com.dipper.monitor.entity.elastic.shard;

import lombok.Data;

import java.util.List;

@Data
public class ShardIndexDistributeView {

    private String indexName;
    // 分片总数
    private Integer shardNum;
    // 主分片数
    private Integer primaryShardNum;
    // 副本分片数
    private Integer replicaShardNum;
    private Integer docNum;
    private Double diskSize;

    // 节点角度的shard分布情况
    private List<ShardEntity> shardIndexViews;
}
