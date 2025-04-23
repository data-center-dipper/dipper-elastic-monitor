package com.dipper.monitor.entity.elastic.shard;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ShardEntity {
    private String index;
    private Integer shard;
    private String prirep;
    private String state;
    private Long docs;
    private String store;
    private String ip;
    private String node;
}
