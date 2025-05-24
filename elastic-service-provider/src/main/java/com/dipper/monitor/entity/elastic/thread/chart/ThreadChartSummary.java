package com.dipper.monitor.entity.elastic.thread.chart;

import lombok.Data;

@Data
public class ThreadChartSummary {
    private String threadType;
    private Double maxValue;
    private Double minValue;
    private Double avgValue;
    private Double sumValue;

    public ThreadChartSummary(String threadType, Double maxValue, Double minValue, Double avgValue, Double sumValue) {
        this.threadType = threadType;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.avgValue = avgValue;
        this.sumValue = sumValue;
    }
}
