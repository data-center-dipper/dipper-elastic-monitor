package com.dipper.monitor.entity.elastic.template.history;

import lombok.Data;

@Data
public class TemplateHistoryView {
    //模板名称
    private String name;
    //模板索引匹配
    private String indexPatterns;
    //模板排序
    private Integer order;
    // 滚动策略
    private String rollingPolicy;
    // 保存策略
    private String storePolicy;
    // 索引个数
    private String indexNum;
    // 自动关闭
    private String closeDays;
    // 详情内容
    private String content;
}
