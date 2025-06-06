package com.dipper.monitor.controller.elastic.index;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.db.elastic.IndexWriteEntity;
import com.dipper.monitor.service.elastic.index.IndexOverviewService;
import com.dipper.monitor.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/index_overview")
@Tag(name = "索引预览", description = "索引预览")
public class IndexOverviewController {

    @Autowired
    private IndexOverviewService indexOverviewService;


    /**
     * 获取当前正在写的索引列表
     */
    @Operation(summary = "获取当前正在写的索引列表",
            description = "获取当前正在写的索引列表",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/writeIndexList")
    public JSONObject writeIndexList() {
        try {
            List<IndexWriteEntity> jsonObject = indexOverviewService.writeIndexList();
            return ResultUtils.onSuccess(jsonObject);
        } catch (IllegalArgumentException e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "获取未来索引列表",
            description = "获取未来索引列表",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/featureIndexList")
    public JSONObject featureIndexList() {
        try {
            List<IndexWriteEntity> jsonObject = indexOverviewService.featureIndexList();
            return ResultUtils.onSuccess(jsonObject);
        } catch (IllegalArgumentException e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }
}
