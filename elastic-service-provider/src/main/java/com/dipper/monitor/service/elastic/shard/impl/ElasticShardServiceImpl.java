package com.dipper.monitor.service.elastic.shard.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import com.dipper.monitor.service.elastic.shard.impl.service.check.CheckShardErrorHandler;
import com.dipper.monitor.service.elastic.shard.impl.service.repair.RepairShardErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ElasticShardServiceImpl implements ElasticShardService {

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
    public String checkShardError() throws Exception {
        CheckShardErrorHandler checkShardErrorHandler = new CheckShardErrorHandler();
        String s = checkShardErrorHandler.checkShardError();
        return s;
    }

    @Override
    public String repairShardError() throws Exception {
        RepairShardErrorHandler repairShardErrorHandler = new RepairShardErrorHandler();
        String s = repairShardErrorHandler.repairShardError();
        return s;
    }
}
