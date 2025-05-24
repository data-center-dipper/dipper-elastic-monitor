package com.dipper.monitor.entity.elastic.shard;

import lombok.Data;

@Data
public class OneShardRepireReq {
    private String indexName;
    private String shardId;
    private Boolean primary = true;
}
