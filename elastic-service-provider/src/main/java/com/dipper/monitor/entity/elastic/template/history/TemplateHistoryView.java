package com.dipper.monitor.entity.elastic.template.history;

import lombok.Data;

@Data
public class TemplateHistoryView {
    // 模板名称
    private String name;

    // 模板索引匹配（多个用逗号分隔）
    private String indexPatterns;

    // 模板排序（优先级）
    private Integer order;

    // 滚动策略
    private String rollingPolicy;

    // 存储策略
    private String storePolicy;

    // 索引分片数量
    private String indexNum;

    // 自动关闭时间（生命周期）
    private String closeDays;

    // 原始内容（JSON 字符串）
    private String content;
}