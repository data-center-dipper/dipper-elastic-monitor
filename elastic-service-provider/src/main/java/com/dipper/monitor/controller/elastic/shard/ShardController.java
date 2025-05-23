package com.dipper.monitor.controller.elastic.shard;


import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.shard.ShardIndexDistributeReq;
import com.dipper.monitor.entity.elastic.shard.ShardIndexDistributeView;
import com.dipper.monitor.entity.elastic.shard.ShardMigrationReq;
import com.dipper.monitor.entity.elastic.shard.ShardNodeDistributeReq;
import com.dipper.monitor.entity.elastic.shard.ShardNodeDistributeView;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import com.dipper.monitor.utils.ResultUtils;
import com.dipper.monitor.utils.Tuple2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/shard_manager")
@Tag(name = "shard管理", description = "shard管理")
public class ShardController {

    @Autowired
    private ElasticShardService elasticShardService;

    @PostMapping("/shardNodeDistribute")
    @Operation(summary = "查看shard分布情况", description = "查看shard分布情况")
    public JSONObject shardDistribute(@RequestBody ShardNodeDistributeReq shardNodeDistributeReq) {
        try {
            Tuple2<Integer, ShardNodeDistributeView> shardDistributeView = elasticShardService.shardNodeDistribute(shardNodeDistributeReq);
            return  ResultUtils.onSuccessWithPageTotal(shardDistributeView.getK(), shardDistributeView.getV());
        } catch (IllegalArgumentException e) {
            log.warn("查看shard分布情况错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("查看shard分布情况失败", e);
            return ResultUtils.onFail(500, "查看shard分布情况失败: " + e.getMessage());
        }
    }

    @PostMapping("/shardIndexDistribute")
    @Operation(summary = "索引角度查看shard分布情况", description = "索引角度查看shard分布情况")
    public JSONObject shardIndexDistribute(@RequestBody ShardIndexDistributeReq shardIndexDistributeReq) {
        try {
            ShardIndexDistributeView shardDistributeView = elasticShardService.shardIndexDistribute(shardIndexDistributeReq);
            return  ResultUtils.onSuccess(shardDistributeView);
        } catch (IllegalArgumentException e) {
            log.warn("查看shard分布情况错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("查看shard分布情况失败", e);
            return ResultUtils.onFail(500, "查看shard分布情况失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/clusterNodes")
    @Operation(summary = "获取集群节点列表", description = "获取集群中所有可用节点列表")
    public JSONObject clusterNodes() {
        try {
            List<String> nodes = elasticShardService.getClusterNodes();
            return ResultUtils.onSuccess(nodes);
        } catch (Exception e) {
            log.error("获取节点列表失败", e);
            return ResultUtils.onFail(500, "获取节点列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/migrate-shard")
    @Operation(summary = "迁移分片", description = "将指定索引的分片从源节点迁移到目标节点")
    public JSONObject migrateShard(@RequestBody ShardMigrationReq migrationReq) {
        try {
            if (migrationReq.getIndex() == null || migrationReq.getShard() == null || 
                migrationReq.getFromNode() == null || migrationReq.getToNode() == null) {
                return ResultUtils.onFail(400, "请提供完整的迁移信息");
            }
            
            boolean success = elasticShardService.migrateShard(migrationReq);
            if (success) {
                return ResultUtils.onSuccess("分片迁移请求已提交");
            } else {
                return ResultUtils.onFail(500, "分片迁移失败");
            }
        } catch (IllegalArgumentException e) {
            log.warn("分片迁移参数错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("分片迁移失败", e);
            return ResultUtils.onFail(500, "分片迁移失败: " + e.getMessage());
        }
    }
    

    
    @PostMapping("/rebalance-node")
    @Operation(summary = "重平衡节点", description = "对指定节点进行分片重平衡操作")
    public JSONObject rebalanceNode(@RequestBody JSONObject params) {
        try {
            String nodeName = params.getString("nodeName");
            if (nodeName == null || nodeName.isEmpty()) {
                return ResultUtils.onFail(400, "节点名称不能为空");
            }
            
            // 调用服务层的重平衡方法
            boolean success = elasticShardService.rebalanceNode(nodeName);
            if (success) {
                return ResultUtils.onSuccess("节点重平衡请求已提交");
            } else {
                return ResultUtils.onFail(500, "节点重平衡失败");
            }
        } catch (IllegalArgumentException e) {
            log.warn("节点重平衡参数错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("节点重平衡失败", e);
            return ResultUtils.onFail(500, "节点重平衡失败: " + e.getMessage());
        }
    }
}
