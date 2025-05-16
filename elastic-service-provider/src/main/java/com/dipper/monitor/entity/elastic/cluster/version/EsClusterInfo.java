package com.dipper.monitor.entity.elastic.cluster.version;

import lombok.Data;

@Data
public class EsClusterInfo {

    private String name;
    private String clusterName;
    private String clusterUuid;
    private Version version;
    private String tagline;
}