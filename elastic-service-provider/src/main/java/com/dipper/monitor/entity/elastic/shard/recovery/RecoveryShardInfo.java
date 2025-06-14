package com.dipper.monitor.entity.elastic.shard.recovery;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 对应 Elasticsearch _cat/recovery 接口返回的每个分片恢复/迁移信息
 */
@Data
public class RecoveryShardInfo {

    private String index;
    private String shard; // 分片编号，字符串类型
    private String time; // 恢复耗时，如 "88ms"
    private String type; // 恢复类型，如 empty_store, peer 等
    private String stage; // 阶段，如 done, index, translog 等

    // 源节点信息
    private String sourceHost;
    private String sourceNode;

    // 目标节点信息
    private String targetHost;
    private String targetNode;

    // 快照相关字段（可能为 n/a）
    private String repository;
    private String snapshot;

    // 文件数量统计
    private Integer files;
    private Integer filesRecovered;
    private BigDecimal filesPercent; // 百分比，例如 0.0%
    private Integer filesTotal;

    // 字节大小统计
    private Long bytes;
    private Long bytesRecovered;
    private BigDecimal bytesPercent;
    private Long bytesTotal;

    // 事务日志操作数
    private Integer translogOps;
    private Integer translogOpsRecovered;
    private BigDecimal translogOpsPercent;

    // Getter / Setter / toString
}