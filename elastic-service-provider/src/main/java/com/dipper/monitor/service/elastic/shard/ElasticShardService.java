package com.dipper.monitor.service.elastic.shard;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.shard.*;
import com.dipper.monitor.entity.elastic.shard.limit.ShardLimitInfo;
import com.dipper.monitor.entity.elastic.shard.overview.ShardRemoveView;
import com.dipper.monitor.entity.elastic.shard.recovery.AllocationEnableReq;
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
    String repairAllShardError() throws Exception;

    /**
     * 修复单个分片异常
     * @param oneShardRepireReq
     * @return
     */
    String repairOneShardError(OneShardRepireReq oneShardRepireReq);


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
    ShardIndexDistributeView shardIndexDistribute(ShardIndexDistributeReq shardIndexDistributeReq) throws IOException;

    /**
     * 获取索引分片信息
     * @param indexName
     */
    List<ShardEntity> getIndexShards(String indexName) throws IOException;
    
    /**
     * 获取集群中所有节点列表
     * @return 节点名称列表
     * @throws IOException
     */
    List<String> getClusterNodes() throws IOException;
    
    /**
     * 迁移分片
     * @param migrationReq 迁移请求参数
     * @return 是否成功提交迁移请求
     * @throws IOException
     */
    boolean migrateShard(ShardMigrationReq migrationReq) throws IOException;

    boolean rebalanceNode(String nodeName);

    /**
     * 查看 shard 迁移情况
     * @param pageReq
     * @return
     */
    Tuple2<Integer, List<ShardRemoveView>> shardIsRemove(PageReq pageReq) throws IOException;


    ShardLimitInfo getClusterShardLimitInfo();
}
