package com.dipper.monitor.entity.elastic.nodes.risk;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ElasticNodeDisk {
    private String name;
    private String transportAddress;
    private Integer shards;
    private String diskIndices;
    private String diskUsed;
    private String diskAvail;
    private String diskTotal;
    private Double diskPercent;
    private String host;
    private String ip;
    private String node;
    private Integer score;
}
