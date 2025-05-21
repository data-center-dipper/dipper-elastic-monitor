package com.dipper.monitor.controller.elastic.index;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.index.IndexListView;
import com.dipper.monitor.entity.elastic.index.IndexPageReq;
import com.dipper.monitor.service.elastic.alians.ElasticAliasService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
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
    @GetMapping("/templateNames")
    public JSONObject templateNames(@RequestParam("nameLike") String nameLike) {
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
    @GetMapping("/aliasNames")
    public JSONObject aliasNames(@RequestParam("nameLike") String nameLike) {
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
    @PostMapping("/indexPageList")
    public JSONObject indexPageList(@RequestBody IndexPageReq indexPageReq) {
        try {
            Tuple2<List<IndexListView>, Long> pageResult = elasticRealIndexService.indexPageList(indexPageReq);
            List<IndexListView> k = pageResult.getK();
            Long v = pageResult.getV();
            return ResultUtils.onSuccessWithPageTotal(v, k);
        } catch (IllegalArgumentException e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }


    @Operation(summary = "查看索引模版信息",
            description = "查看索引模版信息",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/indexTemplate")
    public JSONObject indexTemplate(@RequestParam("indexName") String indexName) {
        try {
            JSONObject jsonObject = elasticRealIndexService.indexTemplate(indexName);
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
