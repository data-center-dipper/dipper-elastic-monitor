package com.dipper.monitor.controller.elastic.template;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.ElasticClusterEntity;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.service.elastic.overview.OverviewService;
import com.dipper.monitor.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/template-data-life")
@Tag(name = "ES模板管理", description = "管理和维护Elasticsearch模板")
public class EsTemplateDataLifeController {

    @Autowired
    private OverviewService overviewService;

    /**
     * 分页获取模版的数据清楚设置
     */
    @Operation(summary = "分页获取模版的数据清楚设置",
            description = "分页获取模版的数据清楚设置",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ElasticClusterEntity.class, type = "array"))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @PostMapping("/getLifeCycleError")
    public JSONObject getLifeCycleError(PageReq pageReq) {
        try {
            List<EsLifeCycleManagement> clusterError = overviewService.getLifeCycleError(pageReq);
            return ResultUtils.onSuccess(clusterError);
        } catch (IllegalArgumentException e) {
            log.error("异常", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("异常", e);
            return ResultUtils.onFail("操作异常");
        }
    }

}
