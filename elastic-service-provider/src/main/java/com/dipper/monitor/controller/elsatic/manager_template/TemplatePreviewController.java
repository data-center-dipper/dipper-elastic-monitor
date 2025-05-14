package com.dipper.monitor.controller.elsatic.manager_template;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.template.TemplatePreviewService;
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

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/template_preview")
@Tag(name = "ES模板预览相关", description = "ES模板预览相关")
public class TemplatePreviewController {

    @Autowired
    private TemplatePreviewService templatePreviewService;

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
            JSONObject jsonObject = templatePreviewService.previewTemplate(esUnconvertedTemplate);
            return ResultUtils.onSuccess(jsonObject);
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    /**
     * 生成一个实时，可用的模版信息，时间会进行替换
     * @param id
     * @return
     */
    @Operation(summary = "根据时间预览ES模板",
            description = "Add a new Elasticsearch template.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template added successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @GetMapping("/previewEffectTemplate/{id}")
    public JSONObject previewEffectTemplate(@PathVariable Integer id) {
        try {
            JSONObject jsonObject = templatePreviewService.previewEffectTemplate(id);
            return ResultUtils.onSuccess(jsonObject);
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }

}
