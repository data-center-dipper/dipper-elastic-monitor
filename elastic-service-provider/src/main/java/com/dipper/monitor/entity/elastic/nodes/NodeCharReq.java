package com.dipper.monitor.entity.elastic.nodes;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class NodeCharReq {
    private Integer nodeId;
    private String timeRange;
    private String startTime;
    private String endTime;
}
