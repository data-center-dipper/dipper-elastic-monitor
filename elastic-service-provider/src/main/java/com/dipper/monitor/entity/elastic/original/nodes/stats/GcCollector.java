package com.dipper.monitor.entity.elastic.original.nodes.stats;

import lombok.Data;

@Data
public class GcCollector {
    private int collection_count;
    private long collection_time_in_millis;
}
