package com.dipper.monitor.entity.db.elastic;

import lombok.Data;

@Data
public class KafkaTopicEntity {
    private String clusterCode; // 属于哪个集群
    private String topicName; // topic名称
    private String topicType = "unknown"; // topic类型
    private String business = "unknown"; // 业务属性
    private Integer partitionNum = -1; // 分区数
    private Integer replication = -1; // 副本数

    private Integer retentionTime = -1; // 保留时间
    private Integer retentionSize = -1; // 保留大小
    private Double diskSize = -1d; // 磁盘大小
    private Long logSize = -1L; // 日志大小 总条目有多少


    private Double lastEps = -1d; // 最近15分钟的eps

    private boolean health = true;
    private String partitionState = "unknown"; // 分区状态


    private String recordSize = "{}"; // 单条数据大小

    private Integer consumerSize = -1; // 消费者总数

    private String configs = ""; // 配置
    private String errorMessage = ""; // 错误消息

    private String updateTime; // 更新时间
    private String createTime; // 创建时间
}