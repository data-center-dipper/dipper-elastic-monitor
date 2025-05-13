package com.dipper.monitor.entity.elastic.template;


import lombok.Data;

import java.util.Date;

/**
 * es模板的详情信息
 */
@Data
public class ElasticTemplateListView   {
    private Integer id;
    private String clusterCode;

    // 模版名称
    private String zhName;
    private String enName;
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
    // 是否开启自动 shard 计算
    private Boolean enableAutoShards;

    private String updateTime;
}
