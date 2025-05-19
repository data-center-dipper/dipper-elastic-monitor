package com.dipper.monitor.entity.elastic.shard;

import lombok.Data;

@Data
public class ShardIndexView {
    // 节点名称
    private String indexName;
    //  状态
    private String status;
    // doc总数
    private Integer docNum;
    // doc总数
    private Double diskSize;

    // doc总数
    private Integer shardNum;
    // 主分片数
    private Double primaryShardNum;
    // 副本因子
    private Integer replicaShardNum;

}
