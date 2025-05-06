package com.dipper.monitor.entity.elastic.cluster.status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shards {
    private int total;
    private int primaries;
    private double replication;
}
