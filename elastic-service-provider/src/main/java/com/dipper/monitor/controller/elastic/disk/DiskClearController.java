package com.dipper.monitor.controller.elastic.disk;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.ElasticClusterEntity;
import com.dipper.monitor.entity.elastic.cluster.ElasticClusterRegisterReq;
import com.dipper.monitor.entity.elastic.disk.GlobalDiskClearReq;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.service.elastic.disk.DiskClearService;
import com.dipper.monitor.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/disk_clear")
@Tag(name = "磁盘清理")
public class DiskClearController {

    @Autowired
    private DiskClearService diskClearService;

    @Operation(summary = "全局清理设置",
            description = "全局清理设置",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JSONObject.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/globalDiskClear")
    public JSONObject globalDiskClear(@RequestBody GlobalDiskClearReq globalDiskClearReq) {
        try {
            diskClearService.globalDiskClear(globalDiskClearReq);
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
            List<ElasticNodeDisk> elasticNodeDisks = diskClearService.nodeDiskTop10();
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
