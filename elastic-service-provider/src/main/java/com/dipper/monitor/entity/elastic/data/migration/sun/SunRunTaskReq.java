package com.dipper.monitor.entity.elastic.data.migration.sun;

import lombok.Data;

@Data
public class SunRunTaskReq {
    private Long parentTaskId;
    private String indexName;
    private String startTime;
    private String endTime;
}