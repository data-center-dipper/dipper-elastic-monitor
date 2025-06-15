package com.dipper.monitor.entity.elastic.data.migration.sun;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SunTaskView {
    private Long id;
    private String parentTaskId;
    private String indexName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Integer retryCount;
    private String errorLog;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}