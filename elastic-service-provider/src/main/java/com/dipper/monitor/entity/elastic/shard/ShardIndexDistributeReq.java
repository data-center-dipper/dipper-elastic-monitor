package com.dipper.monitor.entity.elastic.shard;

import lombok.Data;

@Data
public class ShardIndexDistributeReq {
    // 分页大小
    private Integer pageSize;
    // 当前页码
    private Integer pageNum;
    // 索引名称
    private String indexName;
}
