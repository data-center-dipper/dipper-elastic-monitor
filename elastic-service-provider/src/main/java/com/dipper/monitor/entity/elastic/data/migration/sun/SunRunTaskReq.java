package com.dipper.monitor.entity.elastic.data.migration.sun;

import lombok.Data;

@Data
public class SunRunTaskReq {
    private String parentTaskId;
    private String subTaskId;
}