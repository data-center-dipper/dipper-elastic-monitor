package com.dipper.monitor.entity.elastic.thread;

import lombok.Data;

@Data
public class ThreadCheckItem {
    private String category;    // 检测类别
    private String item;       // 检测项
    private String value;      // 当前值
    private String threshold;  // 阈值
    private String status;     // 状态：正常、警告、严重
    private String description; // 说明
}