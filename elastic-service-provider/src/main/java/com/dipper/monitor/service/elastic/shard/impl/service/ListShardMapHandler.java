package com.dipper.monitor.service.elastic.shard.impl.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.shard.ShardEntity;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ListShardMapHandler {

    private final ElasticClientService elasticClientService;

    public ListShardMapHandler(ElasticClientService elasticClientService) {
        this.elasticClientService = elasticClientService;
    }

    public Map<String, List<ShardEntity>> listShardMap() throws IOException {
        String result = elasticClientService.executeGetApi(ElasticRestApi.SHAED_LIST.getApiPath());

        JSONArray indexDiskJson = JSON.parseArray(result);
        Map<String, List<ShardEntity>> map = new HashMap<>();

        for (Object obj : indexDiskJson) {
            JSONObject shardObj = (JSONObject) obj;

            String state = shardObj.getString("state");
            String index = shardObj.getString("index");

            ShardEntity shard = new ShardEntity()
                    .setIndex(index)
                    .setDocs(shardObj.getLongValue("docs"))
                    .setIp(shardObj.getString("ip"))
                    .setNode(shardObj.getString("node"))
                    .setPrirep(shardObj.getString("prirep"))
                    .setShard(shardObj.getInteger("shard"))
                    .setState(state)
                    .setStore(shardObj.getString("store"));

            // 使用computeIfAbsent方法来避免重复检查key是否存在
            map.computeIfAbsent(index, k -> new ArrayList<>()).add(shard);
        }
        return map;
    }
}