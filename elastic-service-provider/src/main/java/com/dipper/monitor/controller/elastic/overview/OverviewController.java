package com.dipper.monitor.controller.elastic.overview;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.ElasticClusterEntity;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.cluster.ClusterStatusView;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDetail;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.service.elastic.overview.OverviewService;
import com.dipper.monitor.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 集群预览控制器类
 */
@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/elastic_overview")
public class OverviewController {

    @Autowired
    private OverviewService overviewService;

    /**
     * 获取集群状态信息
     */
    @Operation(summary = "获取集群状态信息",
            description = "Returns a list of all managed Kafka clusters.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/clusterStatus")
    public JSONObject clusterStatus() {
        try {
            ClusterStatusView clusterStatusView = overviewService.getClusterStatus();
            return ResultUtils.onSuccess(clusterStatusView);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("异常", e);
            return ResultUtils.onFail("操作异常");
        }
    }

    /**
     * 获取集群生命周期异常列表
     */
    @Operation(summary = "获取集群生命周期异常列表",
            description = "获取集群生命周期异常列表",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/getLifeCycleError")
    public JSONObject getLifeCycleError(@RequestBody  PageReq pageReq) {
        try {
            List<EsLifeCycleManagement>  clusterError = overviewService.getLifeCycleError(pageReq);
            return ResultUtils.onSuccess(clusterError);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("异常", e);
            return ResultUtils.onFail("操作异常");
        }
    }

    /**
     * 生命周期异常检测
     */
    @Operation(summary = "生命周期异常修复",
            description = "生命周期异常修复",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/checkLifeCycleError")
    public JSONObject checkLifeCycleError() {
        try {
            String  clusterError = overviewService.checkLifeCycleError();
            return ResultUtils.onSuccess(clusterError);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("异常", e);
            return ResultUtils.onFail("操作异常");
        }
    }



    /**
     * 生命周期异常修复
     */
    @Operation(summary = "生命周期异常修复",
            description = "生命周期异常修复",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/repairLifeCycleError")
    public JSONObject repairLifeCycleError() {
        try {
            String  clusterError = overviewService.repairLifeCycleError();
            return ResultUtils.onSuccess(clusterError);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("异常", e);
            return ResultUtils.onFail("操作异常");
        }
    }



    /**
     * 获取集群shard异常列表
     */
    @Operation(summary = "获取集群shard异常列表",
            description = "获取集群shard异常列表",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/getShardError")
    public JSONObject getShardError(@RequestBody  PageReq pageReq) {
        try {
            List<JSONObject>  clusterError = overviewService.getShardError(pageReq);
            return ResultUtils.onSuccess(clusterError);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("异常", e);
            return ResultUtils.onFail("操作异常");
        }
    }


    /**
     * shard异常检测
     */
    @Operation(summary = "shard异常修复",
            description = "shard异常修复",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/checkShardError")
    public JSONObject checkShardError() {
        try {
            String  clusterError = overviewService.checkShardError();
            return ResultUtils.onSuccess(clusterError);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("异常", e);
            return ResultUtils.onFail("操作异常");
        }
    }



    /**
     * shard异常修复
     */
    @Operation(summary = "shard异常修复",
            description = "shard异常修复",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/repairShardError")
    public JSONObject repairShardError() {
        try {
            String  clusterError = overviewService.repairShardError();
            return ResultUtils.onSuccess(clusterError);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("异常", e);
            return ResultUtils.onFail("操作异常");
        }
    }




    /**
     * 节点内存负载top10
     */
    @Operation(summary = "节点内存负载top10",
            description = "节点内存负载top10",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/nodeMemoryTop10")
    public JSONObject nodeMemoryTop10() {
        try {
            List<ElasticNodeDetail> elasticNodeDetails = overviewService.nodeMemoryTop10();
            return ResultUtils.onSuccess(elasticNodeDetails);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("异常", e);
            return ResultUtils.onFail("操作异常");
        }
    }

    /**
     * 节点磁盘负载top10
     */
    @Operation(summary = "节点磁盘负载top10",
            description = "节点磁盘负载top10",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/nodeDiskTop10")
    public JSONObject nodeDiskTop10() {
        try {
            List<ElasticNodeDisk> elasticNodeDisks = overviewService.nodeDiskTop10();
            return ResultUtils.onSuccess(elasticNodeDisks);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("异常", e);
            return ResultUtils.onFail("操作异常");
        }
    }





}
