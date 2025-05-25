package com.dipper.monitor.entity.elastic.thread.hot;

import lombok.Data;

import java.util.List;

@Data
public class ThreadHotView {
    private String id;
    private String name;            // 线程名称
    private String type;            // 线程类型

    String nodeGuid ;
    String ip ;
    String hostAndPort ;
    String roles ;
    String attributes ;

    private Integer cpu;            // CPU使用率
    private String memory;          // 内存占用
    private String status;          // 线程状态
    private String createTime;        // 创建时间
    private String description;     // 线程描述
    private String stackTrace;      // 堆栈信息
    private String detail;     // 原始信息



    List<HotThreadMeta> hotThreadMetas;
}
