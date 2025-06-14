package com.dipper.monitor.controller.elastic.shard;


import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.shard.*;
import com.dipper.monitor.entity.elastic.shard.limit.ShardLimitInfo;
import com.dipper.monitor.entity.elastic.shard.overview.ShardRemoveView;
import com.dipper.monitor.entity.elastic.shard.recovery.AllocationEnableReq;
import com.dipper.monitor.service.elastic.setting.ClusterSettingService;
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
    @Autowired
    private ClusterSettingService clusterSettingService;

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

    @GetMapping("/shard-issues")
    @Operation(summary = "获取分片异常列表", description = "获取集群中所有异常分片的列表")
    public JSONObject getShardIssues() {
        try {
            List<JSONObject> shardErrors = elasticShardService.getShardError();
            List<JSONObject> issues = new ArrayList<>();

            // 将分片错误转换为前端需要的格式
            for (JSONObject error : shardErrors) {
                JSONObject issue = new JSONObject();
                issue.put("index", error.getString("index"));
                issue.put("shard", error.getIntValue("shard"));

                // 根据状态生成问题描述
                String state = error.getString("state");
                String prirep = error.getString("prirep");
                String description = "";

                if ("UNASSIGNED".equals(state)) {
                    description = ("p".equals(prirep) ? "主分片" : "副本分片") + "未分配";
                } else if ("INITIALIZING".equals(state)) {
                    description = ("p".equals(prirep) ? "主分片" : "副本分片") + "正在初始化";
                } else if ("RELOCATING".equals(state)) {
                    description = ("p".equals(prirep) ? "主分片" : "副本分片") + "正在重新分配";
                }

                issue.put("issue", description);
                issues.add(issue);
            }

            return ResultUtils.onSuccess(issues);
        } catch (Exception e) {
            log.error("获取分片异常列表失败", e);
            return ResultUtils.onFail(500, "获取分片异常列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/fix-shard-issue")
    @Operation(summary = "修复分片异常", description = "修复指定的分片异常")
    public JSONObject fixShardIssue(@RequestBody OneShardRepireReq oneShardRepireReq) {
        try {
            // 调用服务层的修复方法
            String result = elasticShardService.repairOneShardError(oneShardRepireReq);
            return ResultUtils.onSuccess("分片异常修复操作已执行: " + result);
        } catch (Exception e) {
            log.error("修复分片异常失败", e);
            return ResultUtils.onFail(500, "修复分片异常失败: " + e.getMessage());
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

    @PostMapping("/shardIsRemove")
    @Operation(summary = "查看shard迁移情况", description = "查看shard迁移情况")
    public JSONObject shardIsRemove(@RequestBody PageReq pageReq) {
        try {
            Tuple2<Integer, List<ShardRemoveView>> shardDistributeView = elasticShardService.shardIsRemove(pageReq);
            return  ResultUtils.onSuccessWithPageTotal(shardDistributeView.getK(), shardDistributeView.getV());
        } catch (IllegalArgumentException e) {
            log.warn("查看shard迁移情况: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("查看shard迁移情况", e);
            return ResultUtils.onFail(500, "查看shard迁移情况: " + e.getMessage());
        }
    }

    @PostMapping("/enableOrCloseShardAllocation")
    @Operation(summary = "分片迁移的开启与禁止", description = "分片迁移的开启与禁止")
    public JSONObject enableOrCloseShardAllocation(@RequestBody AllocationEnableReq allocationEnableReq) {
        try {
            clusterSettingService.enableOrCloseShardAllocation(allocationEnableReq);            return  ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.warn("分片迁移的开启与禁止: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("分片迁移的开启与禁止", e);
            return ResultUtils.onFail(500, "分片迁移的开启与禁止: " + e.getMessage());
        }
    }

    @GetMapping("/getShardAllocation")
    @Operation(summary = "获取分片迁移的开启与禁止状态", description = "获取分片迁移的开启与禁止状态")
    public JSONObject getShardAllocation() {
        try {
            String shardAllocation = clusterSettingService.getShardAllocation();
            return  ResultUtils.onSuccess(shardAllocation);
        } catch (IllegalArgumentException e) {
            log.warn("获取分片迁移的开启与禁止状态: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("获取分片迁移的开启与禁止状态", e);
            return ResultUtils.onFail(500, "分片迁移的开启与禁止: " + e.getMessage());
        }
    }



}
