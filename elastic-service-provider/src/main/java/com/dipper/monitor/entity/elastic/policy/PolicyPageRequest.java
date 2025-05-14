package com.dipper.monitor.entity.elastic.policy;

import lombok.Data;

@Data
public class PolicyPageRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
}