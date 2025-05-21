package com.dipper.monitor.entity.elastic.slowsearch;

import lombok.Data;

import java.util.Date;

/**
 * 慢查询终止超时记录
 */
@Data
public class KillTimeoutRecord {
    private Integer id;          // 记录ID
    private Integer queryId;     // 关联的查询ID
    private String indexName;    // 索引名称
    private String queryType;    // 查询类型
    private String killTime;     // 终止时间
    private Long executionTime;  // 执行时间(毫秒)
    private String status;       // 状态
    private String reason;       // 超时原因
    private String nodeId;       // 节点ID
    private String taskId;       // 任务ID
    private String queryContent; // 查询内容
    private Date createTime;     // 创建时间
}