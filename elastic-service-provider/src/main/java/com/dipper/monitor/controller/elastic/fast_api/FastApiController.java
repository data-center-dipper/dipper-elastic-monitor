package com.dipper.monitor.controller.elastic.fast_api;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.fastapi.FastApiDefView;
import com.dipper.monitor.service.elastic.fastapi.FastApiService;
import com.dipper.monitor.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/dipper/monitor/api/v1/elastic/fast_api")
@Tag(name = "快速API", description = "快速API")
public class FastApiController {

    @Autowired
    private FastApiService fastApiService;

    /**
     * 获取所有的API列表信息
     */
    @Operation(summary = "获取所有的API列表信息",
            description = "获取所有的API列表信息",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/fastApiList")
    public JSONObject fastApiList(@RequestParam("nameLike") String nameLike) {
        try {
            List<FastApiDefView> jsonObject = fastApiService.fastApiList(nameLike);
            return ResultUtils.onSuccess(jsonObject);
        } catch (IllegalArgumentException e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    /**
     * 转成curl命令行
     */
    @Operation(summary = "转成curl命令行",
            description = "转成curl命令行",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/transToCurl")
    public JSONObject transToCurl(@RequestBody FastApiDefView fastApiDefView) {
        try {
            String jsonObject = fastApiService.transToCurl(fastApiDefView);
            return ResultUtils.onSuccess(jsonObject);
        } catch (IllegalArgumentException e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    /**
     * 执行命令
     */
    @Operation(summary = "执行命令",
            description = "执行命令",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/executeFastApi")
    public JSONObject executeFastApi(@RequestBody FastApiDefView fastApiDefView) {
        try {
            String jsonObject = fastApiService.executeFastApi(fastApiDefView);
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
