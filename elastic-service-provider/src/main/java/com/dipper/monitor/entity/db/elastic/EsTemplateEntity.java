package com.dipper.monitor.entity.db.elastic;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class EsTemplateEntity {
    private Integer id;
    private String clusterCode;

    // 模版名称
    private String zhName;
    private String enName;

    // 设置信息
    private String settings;
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
    /**
     * 数据生命周期策略
     */
    private String lifePolicy;

    private String templateContent; // JSON内容

    private String statMessage; // 统计信息

    private Date createTime;
    private Date updateTime;
}