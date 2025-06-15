package com.dipper.monitor.entity.db.elastic;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SunTaskEntity {
    private Long id;
    private String parentTaskId;
    private String indexName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String queryContent;
    private String status;
    private Integer retryCount;
    private String errorLog;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
