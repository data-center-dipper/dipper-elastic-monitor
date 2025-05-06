package com.dipper.monitor.entity.elastic.cluster;


import lombok.Data;

@Data
public class CurrentClusterReq {
    private String clusterCode;// 集群唯一代码
    private Boolean currentEnable; // 是否启用当前集群
}
