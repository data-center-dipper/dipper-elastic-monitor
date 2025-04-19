package com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes;

import lombok.Data;

@Data
public class ThreadPool {
    private Long writeQueueSize;
    private Long writeSize;
    // 强制合并（优化索引存储）使用的线程池。
    private Long forceMerge;
    // 搜索协调使用的线程池。
    private Long searchCoordination;
    // 专门用于搜索请求
    private Long search;
}
