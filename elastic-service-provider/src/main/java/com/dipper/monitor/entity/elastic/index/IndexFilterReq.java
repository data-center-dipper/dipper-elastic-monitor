package com.dipper.monitor.entity.elastic.index;

import lombok.Data;

@Data
public class IndexFilterReq {
    private String indexType;
    private String indexAlians;
    private String indexState;
    private Boolean freeze;
    private String healthState;
    private Boolean aliansException;
    private Boolean feature;
}
