package com.dipper.monitor.entity.elastic.original.nodes.stats;

import lombok.Data;

@Data
public class Mem {
    private long total_in_bytes;
    private long free_in_bytes;
    private long used_in_bytes;
    private int free_percent;
    private int used_percent;
}
