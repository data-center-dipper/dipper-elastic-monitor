package com.dipper.monitor.entity.elastic.nodes.service;


import lombok.Data;

@Data
public class EsNodeFailed {
    Integer nodesTotal;
    Integer nodesSuccessful;
    Integer nodesFailed ;
}
