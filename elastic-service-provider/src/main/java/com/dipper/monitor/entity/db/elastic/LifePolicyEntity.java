package com.dipper.monitor.entity.db.elastic;

import lombok.Data;

@Data
public class LifePolicyEntity {
    private Integer id;
    // 中文名称
    private String zhName;
    // 英文名称
    private String enName;
    /**
     * json格式的内容
     **/
    private String policyValue;
}
