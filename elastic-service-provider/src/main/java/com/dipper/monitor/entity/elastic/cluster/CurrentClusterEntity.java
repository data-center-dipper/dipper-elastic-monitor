package com.dipper.monitor.entity.elastic.cluster;


import com.dipper.common.lib.utils.TelnetUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class CurrentClusterEntity {
    private Integer id;
    // 集群的 code 唯一识别码
    private String clusterCode;
    // 集群的服务名称
    private String clusterName;
    // 集群的详情
    private String clusterDesc;
    // 集群的地址
    private String address;
    // 集群的版本
    private String clusterVersion;
    // 是否是当前集群
    private Boolean currentCluster;
    // 是否是 默认集群
    private Boolean defaultCluster;
}

