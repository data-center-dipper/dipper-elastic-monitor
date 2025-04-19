package com.dipper.monitor.service.elastic.shard.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.shard.ShardService;
import com.dipper.monitor.service.elastic.shard.impl.service.RepairShardErrorHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ShardServiceImpl implements ShardService {

    @Autowired
    private ElasticClientService elasticClientService;

    @Override
    public List<JSONObject> getShardError() throws IOException {
        // 检查分片状态
        String shardsResult = elasticClientService.executeGetApi("/_cat/shards?format=json");
        log.info("分片状态：\n{}", shardsResult);

        JSONArray objects = JSONArray.parseArray(shardsResult);

        List<JSONObject> shardError = new ArrayList<>();
        for (Object object : objects) {
            JSONObject jsonObject = (JSONObject) object;
            String state = jsonObject.getString("state");
            if (state.equals("UNASSIGNED") || state.equals("INITIALIZING") || state.equals("RELOCATING")) {
                shardError.add(jsonObject);
            }
        }
        log.info("分片错误：\n{}", shardError.size());
        return shardError;
    }

    @Override
    public String repairShardError() throws Exception {
        RepairShardErrorHandler repairShardErrorHandler = new RepairShardErrorHandler();
        String s = repairShardErrorHandler.repairShardError();
        return s;
    }
}
