package com.dipper.monitor.entity.elastic.index;

import lombok.Data;

@Data
public class IndexPageReq {
    private String pageNum;
    private String pageSize;
    private String templateName;
    private String aliasName;
    private String status;
    private String startTime;
    private String endTime;
    private Boolean featureIndex;
}
