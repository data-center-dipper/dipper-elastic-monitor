package com.dipper.monitor.controller.elsatic.manager_template;


import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.template.ElasticTemplateView;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.template.ElasticPrefabricateTemplateService;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/es-template")
@Tag(name = "ES模板管理", description = "管理和维护Elasticsearch模板")
public class EsTemplateController {

    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;
    @Autowired
    private ElasticRealTemplateService elasticRealTemplateService;


    @Operation(summary = "预览ES模板",
            description = "Add a new Elasticsearch template.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/previewTemplate")
    public JSONObject previewTemplate(@RequestBody EsUnconvertedTemplate esUnconvertedTemplate) {
        try {
            JSONObject jsonObject = elasticRealTemplateService.previewTemplate(esUnconvertedTemplate);
            return ResultUtils.onSuccess(jsonObject);
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "添加ES模板-仅仅保存",
            description = "Add a new Elasticsearch template.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/addTemplate")
    public JSONObject addTemplate(@RequestBody EsUnconvertedTemplate esUnconvertedTemplate) {
        try {
            EsTemplateEntity addedTemplate = elasticStoreTemplateService.addOrUpdateTemplate(esUnconvertedTemplate);
            if (addedTemplate == null) {
                return ResultUtils.onFail("Failed to add template");
            }
            return ResultUtils.onSuccess(addedTemplate);
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "添加ES模板-实时生效",
            description = "Add a new Elasticsearch template.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/addAndRollTemplate")
    public JSONObject addAndRollTemplate(@RequestBody EsUnconvertedTemplate esUnconvertedTemplate) {
        try {
            elasticStoreTemplateService.addAndRollTemplate(esUnconvertedTemplate);
            return ResultUtils.onSuccess();
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }



    @Operation(summary = "获取ES模板详情",
            description = "Retrieve details of an Elasticsearch template by ID.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Template retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "404", description = "Template not found"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/showTemplate")
    public JSONObject showTemplate(@PathVariable Long id) {
        try {
            ElasticTemplateView elasticTemplateView = elasticStoreTemplateService.getTemplateAndStat(id);
            if (elasticTemplateView == null) {
                return ResultUtils.onFail("Template not found");
            }
            return ResultUtils.onSuccess(elasticTemplateView);
        } catch (Exception e) {
            log.error("Error retrieving template", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "更新ES模板",
            description = "Update an existing Elasticsearch template.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Template updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PutMapping("/updateTemplate")
    public JSONObject updateTemplate(@RequestBody EsTemplateEntity esTemplateEntity) {
        try {
            EsTemplateEntity updatedTemplate = elasticStoreTemplateService.updateTemplate(esTemplateEntity);
            if (updatedTemplate == null) {
                return ResultUtils.onFail("Failed to update template");
            }
            return ResultUtils.onSuccess(updatedTemplate);
        } catch (Exception e) {
            log.error("Error updating template", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "删除ES模板",
            description = "Delete an Elasticsearch template by ID.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Template deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Template not found"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @DeleteMapping("/{id}")
    public JSONObject deleteTemplate(@PathVariable Long id) {
        try {
            elasticStoreTemplateService.deleteTemplate(id);
            return ResultUtils.onSuccess();
        } catch (Exception e) {
            log.error("Error deleting template", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "获取所有ES模板",
            description = "Retrieve all Elasticsearch templates.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Templates retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/getAllTemplates")
    public JSONObject getAllTemplates() {
        try {
            List<EsTemplateEntity> templates = elasticStoreTemplateService.getAllTemplates();
            return ResultUtils.onSuccess(templates);
        } catch (Exception e) {
            log.error("Error retrieving templates", e);
            return ResultUtils.onFail("Operation error");
        }
    }
}