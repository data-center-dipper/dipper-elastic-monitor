package com.dipper.monitor.entity.elastic.nodes;

import lombok.Data;

import java.util.Map;

/**
 * 节点详情响应实体类
 */
@Data
public class NodeDetailView {
    private Map<String, Object> basicInfo;
    private String jvmInfo;
    private String diskUsage;
    private String barrelEffect;
    private String threadInfo;
    private String rawInfo;
}