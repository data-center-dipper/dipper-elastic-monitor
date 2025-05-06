package com.dipper.monitor.entity.elastic.original.nodes.stats;

import lombok.Data;

@Data
public class Node {
    private String name;
    private String host;
    private String ip;
    private String transport_address;
    private Indices indices;
    private OS os;
    private Process process;
    private JVM jvm;
}
