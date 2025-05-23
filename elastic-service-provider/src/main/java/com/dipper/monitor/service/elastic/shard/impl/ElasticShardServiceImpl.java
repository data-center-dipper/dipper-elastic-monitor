package com.dipper.monitor.service.elastic.shard.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.entity.elastic.shard.*;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import com.dipper.monitor.service.elastic.shard.impl.handler.ListShardMapHandler;
import com.dipper.monitor.service.elastic.shard.impl.handler.check.CheckShardErrorHandler;
import com.dipper.monitor.service.elastic.shard.impl.handler.repair.RepairShardErrorHandler;
import com.dipper.monitor.service.elastic.shard.impl.handler.views.ShardIndexDistributeViewHandler;
import com.dipper.monitor.service.elastic.shard.impl.handler.views.ShardNodeDistributeViewHandler;
import com.dipper.monitor.utils.Tuple2;
import com.dipper.monitor.utils.mock.MockAllData;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.nio.entity.NStringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ElasticShardServiceImpl implements ElasticShardService {

    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticRealNodeService elasticRealNodeService;

    @Override
    public List<JSONObject> getShardError() throws IOException {
        if (ApplicationUtils.isWindows()) {
            return MockAllData.getShardError();
        }
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
        if (migrationReq.getIndex() == null || migrationReq.getShard() == null ||
                migrationReq.getFromNode() == null || migrationReq.getToNode() == null) {
            throw new IllegalArgumentException("迁移参数不完整");
        }

        if (migrationReq.getFromNode().equals(migrationReq.getToNode())) {
            throw new IllegalArgumentException("源节点和目标节点不能相同");
        }

        if (ApplicationUtils.isWindows()) {
            // 在Windows开发环境中模拟成功
            log.info("模拟分片迁移：{}", JSON.toJSONString(migrationReq));
            return true;
        }

        String requestBody = String.format(
                "{\n" +
                        "  \"commands\": [\n" +
                        "    {\n" +
                        "      \"move\": {\n" +
                        "        \"index\": \"%s\",\n" +
                        "        \"shard\": %d,\n" +
                        "        \"from_node\": \"%s\",\n" +
                        "        \"to_node\": \"%s\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}",
                migrationReq.getIndex(),
                migrationReq.getShard(),
                migrationReq.getFromNode(),
                migrationReq.getToNode()
        );
        try {

            // 创建请求实体
            NStringEntity entity = new NStringEntity(requestBody, org.apache.http.entity.ContentType.APPLICATION_JSON);

            // 执行分片迁移请求
            String response = elasticClientService.executePostApi("/_cluster/reroute", entity);
            log.info("分片迁移响应：{}", response);

            // 检查响应是否包含错误信息
            JSONObject responseJson = JSON.parseObject(response);
            if (responseJson.containsKey("acknowledged") && responseJson.getBoolean("acknowledged")) {
                log.info("分片迁移请求已确认：从 {} 节点迁移索引 {} 的分片 {} 到 {} 节点",
                        migrationReq.getFromNode(),
                        migrationReq.getIndex(),
                        migrationReq.getShard(),
                        migrationReq.getToNode());
                return true;
            } else {
                log.error("分片迁移请求未被确认：{}", response);
                return false;
            }
        } catch (Exception e) {
            log.error("分片迁移失败", e);
            throw new IOException("分片迁移失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean rebalanceNode(String nodeName) {
        return false;
    }
}
