package com.dipper.monitor.entity.elastic.thread.pengding;

import lombok.Data;

@Data
public class PendingTask {
    private Long insertOrder;
    private String priority;
    private String source;
    private Long timeInQueueMillis;
    private Boolean executing;
}