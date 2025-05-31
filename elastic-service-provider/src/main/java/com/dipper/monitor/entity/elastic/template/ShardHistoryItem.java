package com.dipper.monitor.entity.elastic.template;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ShardHistoryItem {
    private String timestamp;
    private Integer shards;
    private Double shardSize;

    public ShardHistoryItem() {

    }

    public ShardHistoryItem(String timestamp, Integer shards, Double shardSize) {
        this.timestamp = timestamp;
        this.shards = shards;
        this.shardSize = shardSize;
    }
}
