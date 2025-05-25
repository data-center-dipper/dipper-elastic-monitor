package com.dipper.monitor.entity.elastic.thread.check;


import lombok.Data;

@Data
public class ThreadPoolCheckItem {
    private String category;       // 线程池名称
    private String item;           // 检查项名称，如 "等待队列长度"
    private String value;          // 当前值
    private String threshold;      // 阈值范围
    private String status;         // 状态：正常 / 警告 / 异常
    private String description;    // 描述信息
}