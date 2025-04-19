package com.dipper.monitor.controller.elsatic.manager_overview;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.ElasticClusterEntity;
import com.dipper.monitor.entity.elastic.cluster.ClusterStatusView;
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
     * 获取集群异常信息
     */
    @Operation(summary = "获取集群异常信息",
            description = "获取集群异常信息",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/clusterError")
    public JSONObject clusterError() {
        try {
            overviewService.clusterError();
            return ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("异常", e);
            return ResultUtils.onFail("操作异常");
        }
    }






}
