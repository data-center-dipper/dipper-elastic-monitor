package com.dipper.monitor.entity.elastic.slowsearch;

import lombok.Data;

/**
 * 慢查询分页请求参数
 */
@Data
public class SlowQueryPageReq {
    private Integer pageNum = 1;      // 页码
    private Integer pageSize = 10;    // 每页大小
    private String searchText;        // 搜索关键词
    private String queryType;         // 查询类型
    private String status;            // 状态
    private String indexName;         // 索引名称
    private Long minExecutionTime;    // 最小执行时间
    private Long maxExecutionTime;    // 最大执行时间
}