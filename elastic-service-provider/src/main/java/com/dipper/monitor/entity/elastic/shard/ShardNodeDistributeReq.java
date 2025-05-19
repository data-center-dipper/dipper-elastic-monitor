package com.dipper.monitor.entity.elastic.shard;

import lombok.Data;

@Data
public class ShardNodeDistributeReq {
    // 分页大小
    private Integer pageSize;
    // 当前页码
    private Integer pageNum;
}
