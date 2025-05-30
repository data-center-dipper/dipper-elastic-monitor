package com.dipper.monitor.entity.db.elastic;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Elasticsearch节点指标实体类
 */
@Data
public class ElasticNodeMetricEntity {
    private Long id;
    private String clusterCode;
    private String nodeId;
    private String nodeName;
    private String hostIp;
    private String transportAddress;
    private String roles;
    
    // CPU指标
    private Integer cpuPercent;
    
    // 内存指标
    private Double osMemTotal;
    private Double osMemFree;
    private Double osMemUsed;
    private Integer osMemUsedPercent;
    private Integer osMemFreePercent;
    private Double jvmMemHeapUsed;
    private Integer jvmMemHeapUsedPercent;
    private Double jvmMemHeapMax;
    
    // 磁盘指标
    private String diskTotal;
    private String diskUsed;
    private String diskAvail;
    private Double diskPercent;
    
    // 文件描述符
    private Integer openFileDescriptors;
    private Integer maxFileDescriptors;
    
    // 线程指标
    private Integer threadsCount;
    
    // 网络指标
    private Long networkRxSize;
    private Long networkRxPackets;
    private Long networkTxSize;
    private Long networkTxPackets;
    
    // IO指标
    private Long ioReadOperations;
    private Long ioWriteOperations;
    private Long ioReadSize;
    private Long ioWriteSize;
    
    // 其他指标
    private Integer shardsCount;
    private Integer indicesCount;
    
    // 时间戳
    private LocalDateTime collectTime;
}