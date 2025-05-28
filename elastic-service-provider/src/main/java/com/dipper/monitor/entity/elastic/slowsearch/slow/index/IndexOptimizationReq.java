package com.dipper.monitor.entity.elastic.slowsearch.slow.index;

import lombok.Data;

@Data
public class IndexOptimizationReq {
    // 索引名称
    private String indexName;
}
