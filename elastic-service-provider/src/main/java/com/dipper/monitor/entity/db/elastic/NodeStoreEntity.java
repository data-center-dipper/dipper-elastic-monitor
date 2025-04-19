package com.dipper.monitor.entity.db.elastic;

import lombok.Data;

import java.util.Date;

@Data
public class NodeStoreEntity {
    private Integer id;
    private String clusterCode;
    private String hostName;
    private String hostIp;
    private Integer hostPort;
    private String address;
}
