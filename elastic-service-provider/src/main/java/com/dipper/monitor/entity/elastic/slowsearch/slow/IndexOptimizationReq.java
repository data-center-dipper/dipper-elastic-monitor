package com.dipper.monitor.entity.elastic.slowsearch.slow;

import lombok.Data;

@Data
public class IndexOptimizationReq {
    // 索引名称
    private String indexName;
}
