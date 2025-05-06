package com.dipper.monitor.entity.elastic.cluster;

import com.dipper.monitor.entity.elastic.cluster.status.ClusterIndices;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterStatsParse {
    private long timestamp;
    private ClusterIndices indices;
}
