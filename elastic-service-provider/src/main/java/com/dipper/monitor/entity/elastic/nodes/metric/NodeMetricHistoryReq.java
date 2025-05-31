package com.dipper.monitor.entity.elastic.nodes.metric;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class NodeMetricHistoryReq {
    private Integer nodeId;
    private Instant startTime;
    private Instant endTime;
    private List<String> metricTypes;
}
