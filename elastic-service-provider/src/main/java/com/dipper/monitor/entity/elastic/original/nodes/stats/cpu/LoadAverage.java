package com.dipper.monitor.entity.elastic.original.nodes.stats.cpu;

import lombok.Data;

@Data
public class LoadAverage {
    private Double _1m;
    private Double _5m;
    private Double _15m;
}
