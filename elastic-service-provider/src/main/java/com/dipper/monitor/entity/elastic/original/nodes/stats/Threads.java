package com.dipper.monitor.entity.elastic.original.nodes.stats;

import lombok.Data;

@Data
public class Threads {
    private int count;
    private int peak_count;
}
