package com.dipper.monitor.entity.elastic.disk;

import lombok.Data;

@Data
public class GlobalDiskClearReq {
    private Integer lowThreshold;
    private Integer mediumThreshold;
    private Integer highThreshold;
}
