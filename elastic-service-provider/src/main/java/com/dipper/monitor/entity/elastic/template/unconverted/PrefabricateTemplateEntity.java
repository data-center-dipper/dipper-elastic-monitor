package com.dipper.monitor.entity.elastic.template.unconverted;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Map;

@Data
public class PrefabricateTemplateEntity {
    // 模版名称
    private Integer order;
    // 模版名称
    private String zhName;
    private String enName;

    // 设置信息
    private Map<String,Object> settings;
    // 使用的字典名称 英文
    private String dicName;
    // 索引匹配模式
    private String indexPatterns;
    // 别名匹配模式
    private String aliansPatterns;
    // 分片数
    private Integer numberOfShards;
    // 副本数
    private Integer numberOfReplicas;

    private String refreshInterval;
    // 是否开启自动 shard 计算
    private Boolean autoShards;
    // 分片大小
    private Integer shardSize;
    // 滚动周期 最小 天 30 60 90 180 365
    private Integer rollingPeriod;

    private JSONObject templateContent; // JSON内容

}
