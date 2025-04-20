package com.dipper.monitor.entity.elastic.template;

import lombok.Data;

import java.util.Date;

@Data
public class EsTemplate {
    private Long id;
    private String clusterCode;
    private String zhName;
    private String enName;
    private String templateContent; // JSON内容
    private Date createTime;
    private Date updateTime;
}