package com.dipper.monitor.entity.elastic.original.nodes.stats;

import lombok.Data;

import java.util.Map;

@Data
public class NodeStatsResponse {
    private Integer total;
    private Integer successful;
    private Integer failed;

    private String cluster_name;

    private Map<String, Node> nodes;
}
