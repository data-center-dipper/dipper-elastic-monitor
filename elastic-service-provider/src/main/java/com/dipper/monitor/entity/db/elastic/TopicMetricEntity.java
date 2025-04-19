package com.dipper.monitor.entity.db.elastic;

import lombok.Data;

@Data
public class TopicMetricEntity {
    private Integer id;
    private Long batchId;
    private String clusterCode;
    private String moduleName;
    private String entityName;
    private String sectionName = "N";
    private String metricKey;
    private Double metricValue;
    private String createTime;
}

