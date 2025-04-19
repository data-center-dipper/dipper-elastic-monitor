package com.dipper.monitor.entity.elastic.cluster;

import lombok.Data;

@Data
public class NodeEntity {
    private String host;
    private Integer port;
    private Integer jmxPort;
    private Integer jmxZkPort;

    public NodeEntity() {

    }

    public NodeEntity(String host, Integer port, Integer jmxPort) {
        this.host = host;
        this.port = port;
        this.jmxPort = jmxPort;
    }
}
