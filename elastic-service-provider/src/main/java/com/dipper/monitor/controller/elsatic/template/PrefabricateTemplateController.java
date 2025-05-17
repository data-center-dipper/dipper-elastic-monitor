package com.dipper.monitor.controller.elsatic.template;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.entity.elastic.template.unconverted.PrefabricateTemplateNames;
import com.dipper.monitor.service.elastic.template.ElasticPrefabricateTemplateService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/prefabricate-template")
@Tag(name = "ES模板管理", description = "管理和维护Elasticsearch模板")
public class PrefabricateTemplateController {

    @Autowired
    private ElasticPrefabricateTemplateService elasticPrefabricateTemplateService;

    @Operation(summary = "获取预制ES模板名称列表",
            description = "获取预制ES模板名称列表",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/prefabricateTemplateNames")
    public JSONObject prefabricateTemplateNames() {
        try {
            List<PrefabricateTemplateNames> jsonObject = elasticPrefabricateTemplateService.prefabricateTemplateNames();
            return ResultUtils.onSuccess(jsonObject);
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    /**
     * 获取某个预制模板
     */
    @GetMapping("/prefabricateOneTemplate")
    public Map<String, Object> prefabricateOneTemplate(@Parameter(description = "enName")  String enName) {
        try {
            EsUnconvertedTemplate nodeStatus = elasticPrefabricateTemplateService.prefabricateOneTemplate(enName);
            return ResultUtils.onSuccess(nodeStatus);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating cluster", e);
            return ResultUtils.onFail("操作异常" + e.getMessage());
        }
    }

    @Operation(summary = "获取预制ES模板",
            description = "获取预制ES模板",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/prefabricateTemplate")
    public JSONObject prefabricateTemplate() {
        try {
            List<EsUnconvertedTemplate> jsonObject = elasticPrefabricateTemplateService.prefabricateTemplate();
            return ResultUtils.onSuccess(jsonObject);
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }
}
