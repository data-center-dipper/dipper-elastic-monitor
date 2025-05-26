package com.dipper.monitor.entity.db.elastic;


import lombok.Data;

import java.util.Date;

@Data
public class SlowQueryEntity {
    private Integer id;          // 主键ID
    private String clusterCode;  // 集群编码
    private String indexName;    // 索引名称
    private String queryType;    // 查询类型：search, aggregation, scroll
    private String startTime;    // 开始时间
    private Long executionTime;  // 执行时间(毫秒)
    private String status;       // 状态：running, completed, killed, failed
    private String nodeId;       // 节点ID
    private String nodeName;     // 节点名称
    private String taskId;       // 任务ID
    private String queryContent; // 查询内容
    private String stackTrace;   // 堆栈信息
    private Date collectTime;    // 收集时间
}
