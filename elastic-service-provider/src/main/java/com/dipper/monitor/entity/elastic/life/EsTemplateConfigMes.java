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
    private Integer countIndex = Integer.valueOf(0);
    private Integer openIndex = Integer.valueOf(0);
    private Integer closeIndex = Integer.valueOf(0);
    private Integer exceptionIndex = Integer.valueOf(0);
    private Integer freezeIndex = Integer.valueOf(0);
    private Integer segmetCount;
    private Long segmentSize;
}
