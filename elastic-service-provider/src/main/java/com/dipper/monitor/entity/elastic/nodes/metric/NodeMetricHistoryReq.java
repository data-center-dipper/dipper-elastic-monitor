package com.dipper.monitor.entity.elastic.nodes.metric;

import lombok.Data;

import java.time.Instant;

@Data
public class NodeMetricHistoryReq {
    private String nodeId;
    private String nodeName;
    private String nodeIp;
    private Instant startTime;
    private Instant endTime;
}
