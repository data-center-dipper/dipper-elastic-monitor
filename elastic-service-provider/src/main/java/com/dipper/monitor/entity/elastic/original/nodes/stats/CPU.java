package com.dipper.monitor.entity.elastic.original.nodes.stats;

import com.dipper.monitor.entity.elastic.original.nodes.stats.cpu.LoadAverage;
import lombok.Data;

@Data
public class CPU {
    private int percent;
    private LoadAverage load_average;
}
