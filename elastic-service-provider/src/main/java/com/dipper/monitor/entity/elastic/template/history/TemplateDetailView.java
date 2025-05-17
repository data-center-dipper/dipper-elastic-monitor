package com.dipper.monitor.entity.elastic.template.history;

import lombok.Data;

import java.util.List;

@Data
public class TemplateDetailView {
    private String name;
    private List<String> indexPatterns;
    private Integer priority;
    private String lifecycleName;
    private String rolloverAlias;
    private String numberOfShards;
    private String numberOfReplicas;
    private String refreshInterval;
    private String codec;
    private String mode;
    private String ignoreMalformed;
    private String slowIndexThreshold;
    private String slowFetchThreshold;
    private String slowQueryThreshold;
    private String content; // 添加content字段用于存储原始JSON数据
}