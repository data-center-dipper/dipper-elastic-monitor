package com.dipper.monitor.entity.elastic.thread.chart;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;


@Data
public class ThreadCharReq {
    private String clusterCode;
    // 节点名称
    String nodeName;
    //  线程类型
    String threadType;
    // 开始时间
    Instant startTime;
    // 结束时间
    Instant endTime;
}
