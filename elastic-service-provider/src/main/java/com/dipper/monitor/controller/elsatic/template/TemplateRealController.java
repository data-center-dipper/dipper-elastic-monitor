package com.dipper.monitor.controller.elsatic.template;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
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

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/template_real")
@Tag(name = "ES模板管理", description = "管理和维护Elasticsearch模板")
public class TemplateRealController {

    @Autowired
    private ElasticRealTemplateService elasticRealTemplateService;

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
    @GetMapping("/rollTemplate/{id}")
    public JSONObject rollTemplate(@PathVariable Integer id) {
        try {
            JSONObject jsonObject = elasticRealTemplateService.rollTemplate(id);
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
