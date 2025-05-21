package com.dipper.monitor.entity.elastic.slowsearch;

import lombok.Data;

/**
 * 慢查询视图对象
 */
@Data
public class SlowQueryView {
    private Integer id;          // 查询ID
    private String indexName;    // 索引名称
    private String queryType;    // 查询类型：search, aggregation, scroll
    private String startTime;    // 开始时间
    private Long executionTime;  // 执行时间(毫秒)
    private String status;       // 状态：running, completed, killed, failed
    private String nodeId;       // 节点ID
    private String taskId;       // 任务ID
    private String queryContent; // 查询内容
    private String stackTrace;   // 堆栈信息（用于详情展示）
}