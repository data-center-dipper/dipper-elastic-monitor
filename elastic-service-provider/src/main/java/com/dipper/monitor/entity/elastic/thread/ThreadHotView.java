package com.dipper.monitor.entity.elastic.thread;

import lombok.Data;
import java.util.Date;

@Data
public class ThreadHotView {
    private Integer id;
    private String name;            // 线程名称
    private String type;            // 线程类型
    private Integer cpu;            // CPU使用率
    private String memory;          // 内存占用
    private String status;          // 线程状态
    private Date createTime;        // 创建时间
    private String description;     // 线程描述
    private String stackTrace;      // 堆栈信息
}