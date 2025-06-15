package com.dipper.monitor.entity.elastic.data.migration;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MigrationTaskView {
    private Long id;
    private String sourceClusterId;
    private String targetClusterId;
    private String indexPattern;
    private String queryCondition;
    private String granularity;
    private Integer nHoursGranularity;
    private Boolean isOnceExecution;
    private String targetIndexPrefix;
    private String executePolicy;
    private Integer concurrencyLimit;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}