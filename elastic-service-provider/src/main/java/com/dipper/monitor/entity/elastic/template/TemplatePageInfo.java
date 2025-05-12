package com.dipper.monitor.entity.elastic.template;

import lombok.Data;

@Data
public class TemplatePageInfo {
    private Integer pageSize;
    private Integer pageNum;
    private String keyword;
}
