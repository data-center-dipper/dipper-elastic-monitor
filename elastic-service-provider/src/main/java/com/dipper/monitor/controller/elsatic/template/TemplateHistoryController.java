package com.dipper.monitor.controller.elsatic.template;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.template.TemplatePageInfo;
import com.dipper.monitor.entity.elastic.template.history.TemplateDetailReq;
import com.dipper.monitor.entity.elastic.template.history.TemplateHistoryView;
import com.dipper.monitor.service.elastic.template.TemplateHistoryService;
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
@RequestMapping("/dipper/monitor/api/v1/elastic/template_history")
@Tag(name = "ES历史模板相关", description = "ES历史模板相关")
public class TemplateHistoryController {

    @Autowired
    private TemplateHistoryService templateHistoryService;

    @Operation(summary = "获取当前使用的模版",
            description = "Retrieve all Elasticsearch templates.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Templates retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/getCurrentUseTemplate")
    public JSONObject getCurrentUseTemplate(@RequestBody TemplatePageInfo templatePageInfo) {
        try {
            Tuple2<Integer,List<TemplateHistoryView>> dics = templateHistoryService.getCurrentUseTemplatePage(templatePageInfo);
            Integer k = dics.getK();
            List<TemplateHistoryView> v = dics.getV();
            return ResultUtils.onSuccessWithPageTotal(k,v);
        } catch (Exception e) {
            log.error("Error retrieving templates", e);
            return ResultUtils.onFail("Operation error");
        }
    }


    @Operation(summary = "获取当前使用的业务模版",
            description = "获取当前使用的业务模版",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Templates retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/getBusenessUseTemplate")
    public JSONObject getBusenessUseTemplate(@RequestBody TemplatePageInfo templatePageInfo) {
        try {
            Tuple2<Integer,List<TemplateHistoryView>> dics = templateHistoryService.getBusenessUseTemplate(templatePageInfo);
            Integer k = dics.getK();
            List<TemplateHistoryView> v = dics.getV();
            return ResultUtils.onSuccessWithPageTotal(k,v);
        } catch (Exception e) {
            log.error("Error retrieving templates", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "获取当前使用的系统模版",
            description = "获取当前使用的系统模版",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Templates retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/getSystemUseTemplate")
    public JSONObject getSystemUseTemplate(@RequestBody TemplatePageInfo templatePageInfo) {
        try {
            Tuple2<Integer,List<TemplateHistoryView>> dics = templateHistoryService.getSystemUseTemplate(templatePageInfo);
            Integer k = dics.getK();
            List<TemplateHistoryView> v = dics.getV();
            return ResultUtils.onSuccessWithPageTotal(k,v);
        } catch (Exception e) {
            log.error("Error retrieving templates", e);
            return ResultUtils.onFail("Operation error");
        }
    }

    @Operation(summary = "获取某个模版详情",
            description = "获取某个模版详情",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Templates retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EsTemplateEntity.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/getOneTemplateDetail")
    public JSONObject getOneTemplateDetail(@RequestBody TemplateDetailReq templateDetailReq) {
        try {
            JSONObject dics = templateHistoryService.getOneTemplateDetail(templateDetailReq);
            return ResultUtils.onSuccess(dics);
        } catch (Exception e) {
            log.error("Error retrieving templates", e);
            return ResultUtils.onFail("Operation error");
        }
    }


}
