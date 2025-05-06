package com.dipper.monitor.entity.elastic.disk;

import lombok.Data;

@Data
public class DiskAllocationInfo {
    private String shards;
    private String diskIndices;
    private String diskUsed;
    private String diskAvail;
    private String diskTotal;
    private String diskPercent;
    private String host;
    private String ip;
    private String node;
}