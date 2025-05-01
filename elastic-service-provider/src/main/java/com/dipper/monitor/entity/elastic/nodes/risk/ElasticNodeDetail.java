package com.dipper.monitor.entity.elastic.nodes.risk;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ElasticNodeDetail {
    private String name;
    private String transportAddress;
    private String roles;
    private String hostIp;
    private Integer cpuPercent;
    private Double osMemTotal;
    private Double osMemFree;
    private Double osMemUsed;
    private Integer osMemusedPercent;
    private Integer osMemFreePercent;
    private Double jvmMemHeapused;
    private Integer jvmMemHeapusedPrecent;
    private Double jvmMemHeapMax;
    private Integer openFileDescriptors;
    private Integer maxFileDescriptors;
    private Integer threadsCount;
    private Integer esDiskThreshouldInt;
    private Integer esJvmThreshouldInt;
    private Integer score;
}
