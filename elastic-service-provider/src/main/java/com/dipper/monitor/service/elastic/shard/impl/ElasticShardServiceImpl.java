package com.dipper.monitor.service.elastic.shard.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.shard.*;
import com.dipper.monitor.entity.elastic.shard.overview.ShardRemoveView;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import com.dipper.monitor.service.elastic.shard.impl.handler.ListShardMapHandler;
import com.dipper.monitor.service.elastic.shard.impl.handler.check.CheckShardErrorHandler;
import com.dipper.monitor.service.elastic.shard.impl.handler.overview.ShardIsRemoveHandler;
import com.dipper.monitor.service.elastic.shard.impl.handler.remove.MigrateShardHandler;
import com.dipper.monitor.service.elastic.shard.impl.handler.repair.RepairAllShardErrorHandler;
import com.dipper.monitor.service.elastic.shard.impl.handler.repair.RepairOneShardErrorHandler;
import com.dipper.monitor.service.elastic.shard.impl.handler.views.ShardIndexDistributeViewHandler;
import com.dipper.monitor.service.elastic.shard.impl.handler.views.ShardNodeDistributeViewHandler;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ElasticShardServiceImpl implements ElasticShardService {

    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticRealNodeService elasticRealNodeService;

    @Override
    public List<JSONObject> getShardError() throws IOException {
//        if (ApplicationUtils.isWindows()) {
//            return MockAllData.getShardError();
//        }
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
    public String repairAllShardError() throws Exception {
        RepairAllShardErrorHandler repairAllShardErrorHandler = new RepairAllShardErrorHandler();
        String s = repairAllShardErrorHandler.repairShardError();
        return s;
    }

    @Override
    public String repairOneShardError(OneShardRepireReq oneShardRepireReq) {
        RepairOneShardErrorHandler repairAllShardErrorHandler = new RepairOneShardErrorHandler();
        return repairAllShardErrorHandler.repairOneShardError(oneShardRepireReq);
    }

    @Override
    public Map<String, List<ShardEntity>> listShardMap() throws IOException {
        ListShardMapHandler listShardMapHandler = new ListShardMapHandler(elasticClientService);
        return listShardMapHandler.listShardMap();
    }

    @Override
    public List<ShardEntity> listShardByPrefix(String indexPatternPrefix, String indexXing) throws IOException {
        String api = "/_cat/shards/" + indexXing + "?format=json";
        log.info("获取某种类型的shard:{}", api);
        String result = this.elasticClientService.executeGetApi(api);

        JSONArray indexDiskJson = JSON.parseArray(result);
        List<ShardEntity> list = new ArrayList<>();
        for (Iterator<Object> nodeItera = indexDiskJson.iterator(); nodeItera.hasNext(); ) {
            JSONObject obj = (JSONObject) nodeItera.next();
            String index = obj.getString("index");
            if (!index.startsWith(indexPatternPrefix)) {
                continue;
            }
            String state = obj.getString("state");

            ShardEntity shard = new ShardEntity();
            shard.setIndex(index)
                    .setDocs(Long.valueOf(obj.getLongValue("docs")))
                    .setIp(obj.getString("ip"))
                    .setNode(obj.getString("node"))
                    .setPrirep(obj.getString("prirep"))
                    .setShard(obj.getInteger("shard"))
                    .setState(state)
                    .setStore(obj.getString("store"));

            list.add(shard);
        }
        return list;
    }


    @Override
    public List<ShardEntity> getIndexShards(String indexName) throws IOException {
        String api = "/_cat/shards/" + indexName + "?format=json";
        log.info("获取某种类型的shard:{}", api);
        String result = this.elasticClientService.executeGetApi(api);

        JSONArray indexDiskJson = JSON.parseArray(result);
        List<ShardEntity> list = new ArrayList<>();
        for (Iterator<Object> nodeItera = indexDiskJson.iterator(); nodeItera.hasNext(); ) {
            JSONObject obj = (JSONObject) nodeItera.next();
            String index = obj.getString("index");

            String state = obj.getString("state");

            ShardEntity shard = new ShardEntity();
            shard.setIndex(index)
                    .setDocs(Long.valueOf(obj.getLongValue("docs")))
                    .setIp(obj.getString("ip"))
                    .setNode(obj.getString("node"))
                    .setPrirep(obj.getString("prirep"))
                    .setShard(obj.getInteger("shard"))
                    .setState(state)
                    .setStore(obj.getString("store"));

            list.add(shard);
        }
        return list;
    }

    @Override
    public Tuple2<Integer, ShardNodeDistributeView> shardNodeDistribute(ShardNodeDistributeReq shardNodeDistributeReq) {
        ShardNodeDistributeViewHandler listShardMapHandler = new ShardNodeDistributeViewHandler(elasticClientService, this);
        return listShardMapHandler.shardNodeDistribute(shardNodeDistributeReq);
    }

    @Override
    public ShardIndexDistributeView shardIndexDistribute(ShardIndexDistributeReq shardIndexDistributeReq) throws IOException {
        ShardIndexDistributeViewHandler listShardMapHandler = new ShardIndexDistributeViewHandler(elasticClientService, this);
        return listShardMapHandler.shardIndexDistribute(shardIndexDistributeReq);
    }

    @Override
    public List<String> getClusterNodes() throws IOException {
        List<String>  nodes =  elasticRealNodeService.getNodeNameList();
        return nodes;
    }

    @Override
    public boolean migrateShard(ShardMigrationReq migrationReq) throws IOException {
        MigrateShardHandler  migrateShardHandler = new MigrateShardHandler(elasticClientService);
        return migrateShardHandler.migrateShard(migrationReq);
    }

    @Override
    public boolean rebalanceNode(String nodeName) {
        return false;
    }

    @Override
    public Tuple2<Integer, List<ShardRemoveView>> shardIsRemove(PageReq pageReq) throws IOException {
        ShardIsRemoveHandler shardIsRemoveHandler = new ShardIsRemoveHandler(this,elasticClientService,elasticRealNodeService);
        return shardIsRemoveHandler.shardIsRemove(pageReq);
    }

}
