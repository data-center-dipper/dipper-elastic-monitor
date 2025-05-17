package com.dipper.monitor.entity.elastic.template.history;

import lombok.Data;

import java.util.List;

@Data
public class EsTemplateInfo {
    private String name;
    private List<String> indexPatterns;
    private Integer order;
    private Integer version;
    private String composedOf;
}