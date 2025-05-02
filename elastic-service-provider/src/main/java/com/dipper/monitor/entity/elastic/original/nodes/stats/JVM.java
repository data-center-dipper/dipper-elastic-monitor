package com.dipper.monitor.entity.elastic.original.nodes.stats;

import lombok.Data;

@Data
public class JVM {
    private JvmMem mem;
    private Threads threads;
    private GC gc;
}
