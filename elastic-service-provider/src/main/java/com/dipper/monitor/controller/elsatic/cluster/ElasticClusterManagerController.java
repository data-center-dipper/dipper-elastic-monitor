package com.dipper.monitor.controller.elsatic.cluster;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.ElasticClusterEntity;
import com.dipper.monitor.entity.elastic.cluster.ClusterDeleteReq;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterReq;
import com.dipper.monitor.entity.elastic.cluster.ElasticClusterRegisterReq;
import com.dipper.monitor.entity.elastic.cluster.ElasticClusterView;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/elastic_manager")
@Tag(name = "kafka集群管理")
public class ElasticClusterManagerController {

    private static final Logger log = LoggerFactory.getLogger(ElasticClusterManagerController.class);

    @Autowired
    private ElasticClusterManagerService elasticClusterManagerService;

    /**
     * 注册一个新的Kafka集群。
     */
    @Operation(summary = "注册一个集群",
            description = "Registers a new Kafka cluster with the provided details.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JSONObject.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/registerCluster")
    public JSONObject registerCluster(
            @Parameter(description = "Details of the Kafka cluster to be registered")
            @RequestBody ElasticClusterRegisterReq clusterRegisterReq) {
        try {
            elasticClusterManagerService.registerCluster(clusterRegisterReq);
            return ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error registering cluster", e);
            return ResultUtils.onFail("操作异常," + e.getMessage());
        }
    }

    /**
     * 更新已有的Kafka集群信息。
     */
    @Operation(summary = "更新集群信息",
            description = "Updates the information of an existing Kafka cluster.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JSONObject.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/updateCluster")
    public JSONObject updateCluster(@RequestBody ElasticClusterRegisterReq clusterRegisterReq) {
        try {
            elasticClusterManagerService.updateCluster(clusterRegisterReq);
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
     * 删除指定的Kafka集群。
     */
    @Operation(summary = "删除集群",
            description = "Deletes the specified Kafka cluster identified by its code.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JSONObject.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/deleteCluster")
    public JSONObject deleteCluster(@RequestBody ClusterDeleteReq clusterDeleteReq) {
        try {
            elasticClusterManagerService.deleteCluster(clusterDeleteReq.getClusterCode());
            return ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting cluster", e);
            return ResultUtils.onFail("操作异常");
        }
    }

    /**
     * 设置默认Kafka集群。
     */
    @Operation(summary = "设置为默认集群",
            description = "Sets the specified Kafka cluster as the default one.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JSONObject.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/setDefaultCluster")
    public JSONObject setDefaultCluster(
            @Parameter(description = "The unique code of the Kafka cluster to be set as default")
            @RequestParam String clusterCode) {
        try {
            elasticClusterManagerService.setDefaultCluster(clusterCode);
            return ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error setting default cluster", e);
            return ResultUtils.onFail("操作异常");
        }
    }

    /**
     * 设置当前使用的Kafka集群。
     */
    @Operation(summary = "设置为当前集群",
            description = "Sets the specified Kafka cluster as the current active one.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JSONObject.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/setCurrentCluster")
    public JSONObject setCurrentCluster(@RequestBody CurrentClusterReq currentClusterReq) {
        try {
            elasticClusterManagerService.setCurrentCluster(currentClusterReq);
            return ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error setting current cluster", e);
            return ResultUtils.onFail("操作异常");
        }
    }

    /**
     * 获取当前使用的Kafka集群详情。
     */
    /**
     * 获取当前使用的Kafka集群详情。
     */
    @Operation(summary = "获取当前集群详情",
            description = "获取当前使用的Kafka集群的详细信息。",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作成功",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class))),
                    @ApiResponse(responseCode = "500", description = "内部服务器错误")
            })
    @PostMapping("/getCurrentCluster")
    public JSONObject getCurrentCluster(
            @Parameter(description = "要获取详情的Kafka集群的唯一代码")
            @RequestParam String clusterCode) {
        try {
            ElasticClusterEntity currentClusterDetail = elasticClusterManagerService.getCurrentClusterDetail(clusterCode);
            return ResultUtils.onSuccess(currentClusterDetail);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching current cluster details for code: {}", clusterCode, e);
            return ResultUtils.onFail("操作异常");
        }
    }


    /**
     * 获取所有Kafka集群列表。
     */
    @Operation(summary = "获取所有集群",
            description = "Returns a list of all managed Kafka clusters.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/getAllCluster")
    public JSONObject getAllCluster() {
        try {
            List<ElasticClusterView> allCluster = elasticClusterManagerService.getAllCluster();
            return ResultUtils.onSuccess(allCluster);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("{}{}{}{}{}{}{}{}get Topics Fail", new Object[]{(new Exception())
                    .getStackTrace()[0].getMethodName(), " #^# ", "query", " #^# ", "FAIL", " #^# ", null, " #^# ", e});
            return ResultUtils.onFail("操作异常");
        }
    }


}
