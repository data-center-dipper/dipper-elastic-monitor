package com.dipper.monitor.entity.elastic.original.nodes.stats;

import lombok.Data;

import java.util.Map;

@Data
public class Nodes {
    private Map<String, Node> nodes;
}
