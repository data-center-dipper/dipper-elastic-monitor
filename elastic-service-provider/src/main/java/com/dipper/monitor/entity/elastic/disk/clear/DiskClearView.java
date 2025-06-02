package com.dipper.monitor.entity.elastic.disk.clear;

import lombok.Data;

@Data
public class DiskClearView {
    // id
    private Integer id;
    // 模版名称
    private String templateName;
    // 最最低磁盘清理预制
    private Integer minLowThreshold;
    // 清理优先级
    private Integer priority;
    // 保留周期
    private Integer retentionPeriod;
    // 保留最低索引个数
    private Integer minIndexSize;
}
