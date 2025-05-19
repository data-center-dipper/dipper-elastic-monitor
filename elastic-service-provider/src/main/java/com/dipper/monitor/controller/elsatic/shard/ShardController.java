package com.dipper.monitor.controller.elsatic.shard;


import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.shard.ShardIndexDistributeReq;
import com.dipper.monitor.entity.elastic.shard.ShardIndexDistributeView;
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
            Tuple2<Integer, ShardIndexDistributeView> shardDistributeView = elasticShardService.shardIndexDistribute(shardIndexDistributeReq);
            return  ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.warn("查看shard分布情况错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("查看shard分布情况失败", e);
            return ResultUtils.onFail(500, "查看shard分布情况失败: " + e.getMessage());
        }
    }
}
