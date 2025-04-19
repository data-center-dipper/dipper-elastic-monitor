package com.dipper.monitor.entity.elastic.cluster;

import lombok.Data;

@Data
public class ElasticClusterView {
    private Integer id;
    // 集群的 code 唯一识别码
    private String clusterCode;
    // 集群的服务名称
    private String clusterName;
    // 集群的详情
    private String clusterDesc;
    // 集群的地址
    private String address;
    // 集群 jmx 的地址
    private String kafkaJmxAddress;
    // 集群的地址
    private String zookeeperAddress;
    // 网络的默认速率
    private Integer networkRate;
    // 是否是当前集群
    private Boolean currentCluster = false;
    // 是否是 默认集群
    private Boolean defaultCluster = false;

    private String clusterPolicy;
    private String monitorStartTime;
    private String monitorEndTime;
}
