package com.dipper.monitor.entity.elastic.shard;


import lombok.Data;

@Data
public class ShardMigrationReq {
    private String index;
    private Integer shard;
    private String fromNode;
    private String toNode;
    private String priority;

}