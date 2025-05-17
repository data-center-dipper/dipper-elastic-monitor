package com.dipper.monitor.controller.elsatic.index;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.service.elastic.alians.ElasticAliasService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/index_manager")
@Tag(name = "索引管理", description = "索引管理")
public class IndexListController {

    @Autowired
    private ElasticRealIndexService elasticRealIndexService;
    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;
    @Autowired
    private ElasticAliasService elasticAliasService;

    /**
     * 获取所有的模版信息
     */
    @Operation(summary = "获取所有的模版信息",
            description = "获取所有的模版信息",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/templateNames/{nameLike}")
    public JSONObject templateNames(@PathVariable String nameLike) {
        try {
            List<String> jsonObject = elasticStoreTemplateService.templateNames(nameLike);
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
     * 获取所有的模版信息
     */
    @Operation(summary = "获取所有的模版信息",
            description = "获取所有的模版信息",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/aliasNames/{nameLike}")
    public JSONObject aliasNames(@PathVariable String nameLike) {
        try {
            List<String> jsonObject = elasticAliasService.aliasNames(nameLike);
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
