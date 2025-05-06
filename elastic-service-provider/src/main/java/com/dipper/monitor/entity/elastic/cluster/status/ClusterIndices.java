package com.dipper.monitor.entity.elastic.cluster.status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterIndices {
    private int count;
    private Shards shards;
    private Docs docs;
    private Store store;
}
