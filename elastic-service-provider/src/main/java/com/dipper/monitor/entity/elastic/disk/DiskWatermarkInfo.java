package com.dipper.monitor.entity.elastic.disk;

import com.alibaba.fastjson.JSONPath;
import lombok.Data;

@Data
public class DiskWatermarkInfo {
    String routingAllocationEnable;
    // routing.allocation.disk.threshould_enabled
    String diskThreshouldEnabled ;
    String updateInterval;
    int highDiskNumber;
    String lowDisk;
}
