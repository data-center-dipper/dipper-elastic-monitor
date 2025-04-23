package com.dipper.monitor.entity.elastic.life;

import lombok.Data;

@Data
public class EsTemplateConfigMes {
    private String templateConfigName;
    private String templateConfigNameValue;
    private String businessName;
    private String rollingCycle;
    private Integer rollingCycleError;
    private Integer shardCount;
    private Integer shardUnassigned;
}
