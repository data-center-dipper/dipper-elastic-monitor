package com.dipper.monitor.entity.elastic.shard.overview;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class ShardRemoveView {
    // 索引名称
    private String indexName;
    // 索引id
    private String shardId;
    // 分片状态 STARTED RELOCATING INITIALIZING UNASSIGNED
    private String shardState;

    // 源节点信息
    private String sourceNode;

    // 目标节点信息
    private String targetNode;

    private String time; // 恢复耗时，如 "88ms"

    private String type; // 恢复类型，如 empty_store, peer 等
    private String stage; // 阶段，如 done, index, translog 等

    private BigDecimal filesPercent; // 百分比，例如 0.0%
    private BigDecimal bytesPercent;

    private BigDecimal translogOpsPercent;

}
