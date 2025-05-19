package com.dipper.monitor.service.elastic.shard.impl.handler.views;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.shard.ShardNodeDistributeReq;
import com.dipper.monitor.entity.elastic.shard.ShardNodeDistributeView;
import com.dipper.monitor.entity.elastic.shard.ShardNodeView;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.shard.impl.ElasticShardServiceImpl;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.util.*;

public class ShardNodeDistributeViewHandler {

    private final ElasticClientService elasticClientService;
    private final ElasticShardServiceImpl elasticShardService;

    public ShardNodeDistributeViewHandler(ElasticClientService elasticClientService, ElasticShardServiceImpl elasticShardService) {
        this.elasticClientService = elasticClientService;
        this.elasticShardService = elasticShardService;
    }

    /**
     * 获取分片分布情况
     */
    public Tuple2<Integer, ShardNodeDistributeView> shardNodeDistribute(ShardNodeDistributeReq shardNodeDistributeReq) {
        try {
            // 1. 获取所有分片信息
            String shardsInfo = elasticClientService.executeGetApi("/_cat/shards?format=json&bytes=b");
            JSONArray jsonArray = JSONArray.parseArray(shardsInfo);

            // 2. 解析分片信息并计算统计数据
            Map<String, ShardNodeView> nodeMap = new HashMap<>();
            int primaryShardCount = 0;
            int replicaShardCount = 0;
            int totalShardCount = jsonArray.size();

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject shardJson = jsonArray.getJSONObject(i);
                String nodeName = shardJson.getString("node");
                boolean isPrimary = "p".equals(shardJson.getString("prirep"));

                if (!nodeMap.containsKey(nodeName)) {
                    ShardNodeView shardNodeView = new ShardNodeView();
                    shardNodeView.setNodeName(nodeName);
                    shardNodeView.setShardNum("0");
                    shardNodeView.setPrimaryShardNum("0");
                    shardNodeView.setReplicaShardNum("0");
                    nodeMap.put(nodeName, shardNodeView);
                }

                ShardNodeView shardNodeView = nodeMap.get(nodeName);
                int currentShardNum = Integer.parseInt(shardNodeView.getShardNum());
                int currentPrimaryShardNum = Integer.parseInt(shardNodeView.getPrimaryShardNum());
                int currentReplicaShardNum = Integer.parseInt(shardNodeView.getReplicaShardNum());

                shardNodeView.setShardNum(String.valueOf(currentShardNum + 1));
                if (isPrimary) {
                    primaryShardCount++;
                    shardNodeView.setPrimaryShardNum(String.valueOf(currentPrimaryShardNum + 1));
                } else {
                    replicaShardCount++;
                    shardNodeView.setReplicaShardNum(String.valueOf(currentReplicaShardNum + 1));
                }
            }

            // 3. 组装最终视图对象
            ShardNodeDistributeView shardNodeDistributeView = new ShardNodeDistributeView();
            shardNodeDistributeView.setNodeNum(String.valueOf(nodeMap.size()));
            shardNodeDistributeView.setShardNum(String.valueOf(totalShardCount));
            shardNodeDistributeView.setPrimaryShardNum(String.valueOf(primaryShardCount));
            shardNodeDistributeView.setReplicaShardNum(String.valueOf(replicaShardCount));
            shardNodeDistributeView.setShardNodeViews(new ArrayList<>(nodeMap.values()));

            return new Tuple2<>(200, shardNodeDistributeView);
        } catch (IOException e) {
            e.printStackTrace();
            return new Tuple2<>(500, null);
        }
    }
}