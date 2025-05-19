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
import java.util.List;

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
    public Tuple2<Integer, ShardIndexDistributeView> shardIndexDistribute(ShardIndexDistributeReq req) {
        String indexName = req.getIndexName();
        if (indexName == null || indexName.trim().isEmpty()) {
            return new Tuple2<>(400, null); // 参数校验失败
        }

        try {
            List<ShardEntity> indexShards = elasticShardService.getIndexShards(indexName);
            int primaryCount = getPrimaryCount(indexShards);
            int replicaCount = getReplicaCount(indexShards);

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

            return new Tuple2<>(200, result);

        } catch (IOException e) {
            e.printStackTrace();
            return new Tuple2<>(500, null);
        }
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