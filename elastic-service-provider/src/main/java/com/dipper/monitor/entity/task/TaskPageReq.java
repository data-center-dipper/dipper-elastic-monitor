package com.dipper.monitor.entity.task;

import lombok.Data;

@Data
public class TaskPageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
}
