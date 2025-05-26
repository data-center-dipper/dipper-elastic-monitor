package com.dipper.monitor.entity.elastic.slowsearch;

import lombok.Data;

import java.util.Date;

/**
 * 表示一个慢查询任务的实体类
 */
@Data
public class SlowQueryTaskEntity {
    private String clusterCode;         // 集群编码
    private String nodeId;              // 节点ID
    private String nodeName;            // 节点名称
    private String taskId;              // 任务ID（唯一标识）
    private String action;              // 任务类型（如 search:query）
    private String queryType;           // 查询类型（如 search, scroll, aggregation）
    private long executionTime;         // 执行时间（毫秒）
    private String startTime;           // 开始时间（ISO格式字符串）
    private String description;         // 查询描述（DSL语句）
    private String status;              // 状态（completed / running）
    private Date collectTime;           // 收集时间
}