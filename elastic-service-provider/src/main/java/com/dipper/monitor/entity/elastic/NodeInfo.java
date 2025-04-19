package com.dipper.monitor.entity.elastic;

import lombok.Data;

/**
 * 节点信息实体类
 */
@Data
public class NodeInfo {
    private Integer id;
    private String name;
    private String ip;
    private Integer port;
    private String onlineStatus;
    private String connectStatus;
    private String jvmInfo;
    private String diskUsage;
    private Integer threadCount;
    private String barrelEffect;
}