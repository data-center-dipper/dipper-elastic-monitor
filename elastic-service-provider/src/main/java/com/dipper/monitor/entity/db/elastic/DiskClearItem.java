package com.dipper.monitor.entity.db.elastic;

import lombok.Data;

import java.util.Date;

@Data
public class DiskClearItem {
    private Integer id;
    private String templateName;
    private Integer minLowThreshold;
    private Integer priority;
    private Integer retentionPeriod;
    private Integer minIndexSize;
    private Date createTime;
    private Date updateTime;
}