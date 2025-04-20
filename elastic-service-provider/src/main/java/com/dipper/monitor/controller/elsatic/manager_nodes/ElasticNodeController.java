package com.dipper.monitor.controller.elsatic.manager_nodes;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.LineChartDataResponse;
import com.dipper.monitor.entity.elastic.nodes.*;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.utils.ResultUtils;
import com.dipper.monitor.utils.Tuple2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/node_manager")
@Tag(name = "Elastic节点管理")
public class ElasticNodeController {

    @Autowired
    private ElasticRealNodeService elasticRealNodeService;

    /**
     * 获取节点的简要信息
     */
    @Operation(summary = "nodePageList",
            description = "nodePageList",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JSONObject.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/nodePageList")
    public JSONObject nodePageList(@RequestBody NodeInfoReq nodeInfoReq) {
        try {
            Tuple2<List<OneNodeTabView>, Integer> nodeStatus = elasticRealNodeService.nodePageList(nodeInfoReq);
            if(nodeStatus == null){
                return ResultUtils.onSuccessWithPageTotal(0, Collections.emptyList());
            }
            return ResultUtils.onSuccessWithPageTotal(nodeStatus.getV(),nodeStatus.getK());
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating cluster", e);
            return ResultUtils.onFail("操作异常" + e.getMessage());
        }
    }



    /**
     * 获取节点详情信息
     * @param nodeId 节点 ID
     * @return 节点详情响应数据
     */
    @GetMapping("/getOneNodeView")
    public Map<String, Object> getOneNodeView(@RequestParam Integer nodeId) {
        try {
            NodeDetailView nodeStatus = elasticRealNodeService.getOneNodeView(nodeId);
            return ResultUtils.onSuccess(nodeStatus);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating cluster", e);
            return ResultUtils.onFail("操作异常" + e.getMessage());
        }
    }

    /**
     * 获取节点原始详情信息
     * @param nodeId 节点 ID
     * @return 节点详情响应数据
     */
    @GetMapping("/getOneNodeView")
    public Map<String, Object> getOneNodeOriginal(@RequestParam Integer nodeId) {
        try {
            JSONObject nodeStatus = elasticRealNodeService.getOneNodeOriginal(nodeId);
            return ResultUtils.onSuccess(nodeStatus);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating cluster", e);
            return ResultUtils.onFail("操作异常" + e.getMessage());
        }
    }

    /**
     * 删除节点
     * @param nodeId 节点 ID
     * @return 删除操作结果
     */
    @PostMapping("/nodesDelete")
    public Map<String, Object> deleteNode(@RequestParam Integer nodeId) {
        try {
            elasticRealNodeService.deleteNode(nodeId);
            return ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating cluster", e);
            return ResultUtils.onFail("操作异常" + e.getMessage());
        }
    }


    /**
     * 修改节点信息
     * @return 修改操作结果
     */
    @PostMapping("/nodesUpdate")
    public Map<String, Object> updateNode(NodeUpdateReq nodeUpdateReq) {
        try {
            elasticRealNodeService.updateNode(nodeUpdateReq);
            return ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating cluster", e);
            return ResultUtils.onFail("操作异常" + e.getMessage());
        }
    }


    /**
     * 获取折线图数据
     * @return 折线图数据响应
     */
    @GetMapping("/nodesLineChart")
    public Map<String, Object> getLineChartData(NodeCharReq nodeCharReq) {
        try {
            LineChartDataResponse response = elasticRealNodeService.getLineChartData(nodeCharReq);
            return ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating cluster", e);
            return ResultUtils.onFail("操作异常" + e.getMessage());
        }
    }

}
