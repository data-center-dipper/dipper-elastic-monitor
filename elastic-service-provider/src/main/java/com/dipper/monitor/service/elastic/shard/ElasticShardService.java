package com.dipper.monitor.service.elastic.shard;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.shard.*;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ElasticShardService {
    /**
     * 获取分片异常列表
     * @return
     * @throws IOException
     */
    List<JSONObject> getShardError() throws IOException;

    /**
     * 分片异常检测
     * @return
     */
    String checkShardError() throws Exception;

    /**
     * 修复分片异常
     * @return
     */
    String repairShardError() throws Exception;


    Map<String, List<ShardEntity>> listShardMap() throws IOException;

    List<ShardEntity> listShardByPrefix(String indexPatternPrefix, String indexXing) throws IOException;

    /**
     * 节点角度查看shard分布情况
     */
    Tuple2<Integer, ShardNodeDistributeView> shardNodeDistribute(ShardNodeDistributeReq shardNodeDistributeReq);

    /**
     * 索引角度查看shard分布情况
     * @param shardIndexDistributeReq
     * @return
     */
    Tuple2<Integer, ShardIndexDistributeView> shardIndexDistribute(ShardIndexDistributeReq shardIndexDistributeReq);

    /**
     * 获取索引分片信息
     * @param indexName
     */
    List<ShardEntity> getIndexShards(String indexName) throws IOException;
}
