package com.dipper.monitor.entity.elastic.policy;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class InnerPolicyEntity {
    private String zhName;
    private String enName;
    private JSONObject policyContent;
}
