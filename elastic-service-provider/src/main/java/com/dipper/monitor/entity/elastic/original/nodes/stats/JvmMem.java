package com.dipper.monitor.entity.elastic.original.nodes.stats;

import lombok.Data;

@Data
public class JvmMem {
    private long heap_used_in_bytes;
    private int heap_used_percent;
    private long heap_committed_in_bytes;
    private long heap_max_in_bytes;
    private long non_heap_used_in_bytes;
    private long non_heap_committed_in_bytes;
}
