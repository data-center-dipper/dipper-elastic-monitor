package com.dipper.monitor.entity.elastic.disk.clear;

import lombok.Data;

@Data
public class DiskClearPageReq {
    private Integer pageNum;
    private Integer pageSize;
    private String keyWord;
}
