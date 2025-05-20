package com.dipper.monitor.entity.elastic.thread;

import lombok.Data;

@Data
public class ThreadPageReq {
    private Integer pageNum = 1;     // 页码
    private Integer pageSize = 10;   // 每页大小
    private String searchText;       // 搜索关键词
    private String threadType;       // 线程类型
    private String threadStatus;     // 线程状态
}