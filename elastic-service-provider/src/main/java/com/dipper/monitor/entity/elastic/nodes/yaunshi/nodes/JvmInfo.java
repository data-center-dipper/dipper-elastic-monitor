package com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes;

import lombok.Data;

@Data
public class JvmInfo {
    private String pid;
    private Long startTimeInMillis;
    private Long heapInitInBytes;
    private Long heapMaxInBytes;
    // 非堆内存的最大大小，如果设置为0，意味着没有明确限制。
    private Long nonHeapMaxInBytes;
    private Long directMaxInBytes;
}
