package com.dipper.monitor.entity.nodes;

import lombok.Data;

import java.util.Date;

@Data
public class NodeRuntime {
    private Long id;
    private String clusterCode;
    private String module;
    private String hostName;
    private String hostIp;
    private Long runTime;
    private java.util.Date createTime;
    private java.util.Date updateTime;

    public NodeRuntime() {
        super();
    }

    public NodeRuntime(Long id, String clusterCode,String module, String hostName, String hostIp, Long runTime, java.util.Date createTime, java.util.Date updateTime) {
        this.id = id;
        this.clusterCode = clusterCode;
        this.module = module;
        this.hostName = hostName;
        this.hostIp = hostIp;
        this.runTime = runTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}