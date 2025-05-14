package com.dipper.monitor.entity.elastic.policy.response;

import lombok.Data;
import java.util.Date;

@Data
public class LifePolicyResponse {
    private Integer id;
    private String clusterCode;
    private String zhName;
    private String enName;
    private String policyContent;
    private String updateTime;
}