package com.dipper.monitor.entity.elastic.slowsearch.kill;

import lombok.Data;

import java.util.Date;

@Data
public class KillQueryResult {
    private boolean success;
    private String reason;
    private Integer queryId;
    private String taskId;
    private Date killTime = new Date();
}