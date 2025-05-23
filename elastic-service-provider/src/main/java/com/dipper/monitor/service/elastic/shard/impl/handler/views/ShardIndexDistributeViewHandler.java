package com.dipper.monitor.service.elastic.shard.impl.handler.views;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.shard.ShardEntity;
import com.dipper.monitor.entity.elastic.shard.ShardIndexDistributeReq;
import com.dipper.monitor.entity.elastic.shard.ShardIndexDistributeView;
import com.dipper.monitor.entity.elastic.shard.ShardIndexView;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 索引角度查看 shard分布视图处理器
 */
public class ShardIndexDistributeViewHandler {

    private final ElasticClientService elasticClientService;
    private final ElasticShardService elasticShardService;

    public ShardIndexDistributeViewHandler(ElasticClientService elasticClientService, ElasticShardService elasticShardService) {
        this.elasticClientService = elasticClientService;
        this.elasticShardService = elasticShardService;
    }

    /**
     * 获取指定索引的分片分布详情（按索引维度查看）
     */
    public ShardIndexDistributeView shardIndexDistribute(ShardIndexDistributeReq req) throws IOException {
        String indexName = req.getIndexName();
        if (indexName == null || indexName.trim().isEmpty()) {
            throw new IllegalArgumentException("indexName is empty");
        }

        List<ShardEntity> indexShards = elasticShardService.getIndexShards(indexName);
        int primaryCount = getPrimaryCount(indexShards);
        int replicaCount = getReplicaCount(indexShards);
        List<ShardEntity> indexNodeShards = getShardByGroupNode(indexShards);

        // 获取索引的基本信息
        String statsApi = "/_cat/indices/" + indexName + "?format=json&bytes=b";
        String indicesInfo = elasticClientService.executeGetApi(statsApi);
        JSONArray indicesArray = JSONArray.parseArray(indicesInfo);
        JSONObject indexStats = indicesArray.isEmpty() ? new JSONObject() : indicesArray.getJSONObject(0);

        ShardIndexDistributeView result = new ShardIndexDistributeView();
        result.setIndexName(indexName);
        result.setShardNum(primaryCount + replicaCount);
        result.setPrimaryShardNum(primaryCount);
        result.setReplicaShardNum(replicaCount);
        result.setDocNum(indexStats.getIntValue("docs.count"));
        result.setDiskSize(Double.parseDouble(indexStats.getString("store.size")));
        result.setShardIndexViews(indexShards);
        result.setShardNodesViews(indexNodeShards);

        return result;


    }


    /**
     * 按照节点对分片进行统计（传统 for 循环版本）
     * @param indexShards 输入的分片实体列表
     * @return 根据节点分组并统计后的分片实体列表
     */
    private List<ShardEntity> getShardByGroupNode(List<ShardEntity> indexShards) {
        Map<String, ShardEntity> resultMap = new HashMap<>();

        for (ShardEntity shard : indexShards) {
            String node = shard.getNode();

            if (resultMap.containsKey(node)) {
                // 如果已经存在该节点，累加 shard 和 docs
                ShardEntity existing = resultMap.get(node);
                existing.setShard(existing.getShard() + shard.getShard());
                existing.setDocs(existing.getDocs() + shard.getDocs());
            } else {
                // 否则新建一个条目，只复制需要保留字段
                ShardEntity entity = new ShardEntity();
                entity.setIndex(shard.getIndex())
                        .setShard(shard.getShard())
                        .setDocs(shard.getDocs())
                        .setNode(shard.getNode());
                resultMap.put(node, entity);
            }
        }

        // 返回结果集合
        return new ArrayList<>(resultMap.values());
    }

    /**
     * 统计副本分片数量 (prirep = 'r')
     */
    private int getReplicaCount(List<ShardEntity> indexShards) {
        if (indexShards == null || indexShards.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (ShardEntity shard : indexShards) {
            if ("r".equalsIgnoreCase(shard.getPrirep())) {
                count++;
            }
        }
        return count;
    }

    /**
     * 统计主分片数量 (prirep = 'p')
     */
    private int getPrimaryCount(List<ShardEntity> indexShards) {
        if (indexShards == null || indexShards.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (ShardEntity shard : indexShards) {
            if ("p".equalsIgnoreCase(shard.getPrirep())) {
                count++;
            }
        }
        return count;
    }
}