package com.dipper.monitor.entity.elastic.thread.check.pool;

import lombok.Data;

/**
 * 每个线程池的趋势分析结果
 */
@Data
public class ThreadPoolTrendResult {
    private String nodeName;
    private String poolName;
    private int lastActive;
    private int lastQueue;
    private boolean isQueueIncreasing;
    private boolean isActiveIncreasing;

    public ThreadPoolTrendResult(String nodeName, String poolName, int lastActive, int lastQueue, boolean isQueueIncreasing, boolean isActiveIncreasing) {
        this.nodeName = nodeName;
        this.poolName = poolName;
        this.lastActive = lastActive;
        this.lastQueue = lastQueue;
        this.isQueueIncreasing = isQueueIncreasing;
        this.isActiveIncreasing = isActiveIncreasing;
    }
}