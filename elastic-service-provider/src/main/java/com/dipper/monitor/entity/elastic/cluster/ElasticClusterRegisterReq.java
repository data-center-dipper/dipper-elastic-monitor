package com.dipper.monitor.entity.elastic.cluster;

import lombok.Data;

@Data
public class ElasticClusterRegisterReq {
    // 集群的 code 唯一识别码
    private String clusterCode;
    // 集群的服务名称
    private String clusterName;
    // 集群的详情
    private String clusterDesc;
    // 集群的地址
    private String address;
    // 是否忽略 校验 地址
    private Boolean checkAddress = false;
}
