package com.dipper.monitor.entity.elastic.shard.limit;

import lombok.Data;

@Data
public class ShardLimitInfo {
    private Integer shardLimit;
    private Integer currentShards;
}
