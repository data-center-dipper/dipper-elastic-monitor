package com.dipper.monitor.entity.elastic.original.nodes.stats;

import lombok.Data;

import java.util.Map;

@Data
public class GC {
    private Map<String, GcCollector> collectors;
}
