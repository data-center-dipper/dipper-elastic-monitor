package com.dipper.monitor.entity.elastic.thread;

import lombok.Data;

@Data
public class ThreadPageReq {
    private Integer pageNum = 1;     // 页码
    private Integer pageSize = 10;   // 每页大小
}