package com.dipper.monitor.entity.elastic.thread.check;

import lombok.Data;

@Data
public class ThreadPoolStat {
    private String name;      // 线程池名称，如 bulk, index, search
    private int size;         // 最大线程数
    private int active;       // 活跃线程数
    private int queue;        // 队列任务数
    private int rejected;     // 被拒绝的任务数
}