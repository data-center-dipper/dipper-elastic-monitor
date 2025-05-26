package com.dipper.monitor.entity.elastic.slowsearch;

import lombok.Data;

import java.util.Date;

/**
 * 慢查询视图对象
 */
@Data
public class SlowQueryView {
    private Long id; // 主键ID

    // 集群 & 节点信息
    private String clusterCode; // 集群编码
    private String nodeId;      // 节点ID
    private String nodeName;    // 节点名称

    // 任务信息
    private String taskId;      // 任务ID
    private String action;      // 任务类型（如 search:query）
    private String queryType;   // 查询类型（search, aggregation, scroll）

    // 查询上下文
    private String indexName;   // 索引名称（多个用逗号分隔）
    private String description; // 任务描述（DSL语句摘要）
    private String queryContent; // 查询内容（完整DSL或提取的条件）

    // 执行信息
    private Date startTime;     // 开始时间
    private Long executionTimeMs; // 执行耗时（毫秒）
    private String status;      // 状态（running, completed, killed, failed）

    // 错误/调试信息
    private String stackTrace;  // 堆栈信息（用于异常追踪）

    // 元信息
    private Date collectTime;   // 数据采集时间
    private Integer isProcessed; // 是否已处理（0=未处理，1=已处理）


}