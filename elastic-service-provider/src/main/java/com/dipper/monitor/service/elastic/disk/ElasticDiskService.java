package com.dipper.monitor.service.elastic.disk;

import com.dipper.monitor.entity.elastic.disk.DiskAllocationInfo;
import com.dipper.monitor.entity.elastic.disk.DiskWatermarkInfo;

import java.io.IOException;
import java.util.List;

public interface ElasticDiskService {
    /**
     * 获取磁盘洪水阈值
     */
    DiskWatermarkInfo getDiskWatermark() throws IOException;

    List<DiskAllocationInfo> getDiskAllocation();
}
