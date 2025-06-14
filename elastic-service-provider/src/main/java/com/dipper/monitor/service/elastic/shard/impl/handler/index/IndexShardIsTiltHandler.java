package com.dipper.monitor.service.elastic.shard.impl.handler.index;

import com.dipper.monitor.entity.elastic.shard.ShardEntity;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class IndexShardIsTiltHandler {

    private final ElasticShardService elasticShardService;

    public IndexShardIsTiltHandler(ElasticShardService elasticShardService) {
        this.elasticShardService = elasticShardService;
    }

    /**
     * 判断指定索引是否存在分片倾斜（主分片集中在一两个节点上）
     *
     * @param indexName 索引名称
     * @return true 表示存在分片倾斜，false 表示分布均匀
     */
    public boolean isIndexTilt(String indexName) throws IOException {
        List<ShardEntity> shards = elasticShardService.getIndexShards(indexName);

        if (shards == null || shards.isEmpty()) {
            log.warn("未找到索引 [{}] 的分片信息", indexName);
            return false;
        }

        // 统计每个节点上的主分片数量
        Map<String, Integer> primaryShardCountMap = new HashMap<>();

        for (ShardEntity shard : shards) {
            if ("p".equalsIgnoreCase(shard.getPrirep())) { // 只统计主分片
                String node = shard.getNode();
                if (node != null && !node.isEmpty()) {
                    primaryShardCountMap.put(node, primaryShardCountMap.getOrDefault(node, 0) + 1);
                }
            }
        }

        int totalPrimaryShards = primaryShardCountMap.values().stream().mapToInt(Integer::intValue).sum();
        int nodeCount = primaryShardCountMap.size();

        if (nodeCount <= 0) {
            log.warn("索引 [{}] 的主分片未分配到任何节点", indexName);
            return false;
        }

        double avgPerNode = (double) totalPrimaryShards / nodeCount;
        double threshold = avgPerNode * 2; // 超过平均值2倍则视为倾斜

        for (Map.Entry<String, Integer> entry : primaryShardCountMap.entrySet()) {
            if (entry.getValue() > threshold) {
                log.warn("检测到分片倾斜：节点 [{}] 上有 {} 个主分片，平均为 {:.1f}",
                        entry.getKey(), entry.getValue(), avgPerNode);
                return true;
            }
        }

        log.info("索引 [{}] 分布均匀，无明显分片倾斜", indexName);
        return false;
    }
}