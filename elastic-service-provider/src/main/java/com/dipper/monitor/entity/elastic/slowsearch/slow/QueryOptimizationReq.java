package com.dipper.monitor.entity.elastic.slowsearch.slow;

import lombok.Data;

@Data
public class QueryOptimizationReq {
    // 查询ID，用于获取具体查询详情
    private String queryId;
    // 索引名称
    private String indexName;
    // 查询类型（search、aggregation、scroll等）
    private String queryType;
    // 查询内容，可选参数
    private String searchText;
}
