package com.dipper.monitor.entity.elastic.policy;

import lombok.Data;

@Data
public class LifePolicyRequest {
    private Integer id;
    private String zhName;
    private String enName;
    private String policyContent;
}