package com.dipper.monitor.entity.db.elastic;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ThreadMetricEntity {
    private Long id;
    private String clusterCode;
    private String nodeName;
    private String threadType;
    private Integer activeThreads;
    private Integer queueSize;
    private Long rejectedCount;
    private Long completedCount;
    private Integer largestSize;
    private Double cpuUsage;
    private Long memoryUsage;
    private LocalDateTime collectTime;
}
