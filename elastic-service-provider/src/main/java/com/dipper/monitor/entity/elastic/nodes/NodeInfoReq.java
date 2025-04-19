package com.dipper.monitor.entity.elastic.nodes;

import lombok.Data;

@Data
public class NodeInfoReq {
    private Integer pageSize;
    private Integer pageNum;
}