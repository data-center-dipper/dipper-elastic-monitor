package com.dipper.monitor.entity.elastic.nodes;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class NodeUpdateReq {
    private Integer nodeId;
    private String nodeName;
    private String nodeIp;
    private Integer nodePort;
}
