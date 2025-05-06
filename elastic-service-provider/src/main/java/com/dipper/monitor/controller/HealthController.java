package com.dipper.monitor.controller;

import com.dipper.common.lib.dubbo.DubboResult;
import com.dipper.monitor.service.health.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "健康检查", description = "提供服务的健康检查API.")
@Slf4j
public class HealthController {

    @Autowired
    private HealthService healthService;

    /**
     * 执行健康检查。
     */
    @Operation(summary = "执行健康检查",
            description = "用于验证服务是否正常运行。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "服务健康检查成功",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = DubboResult.class))),
                    @ApiResponse(responseCode = "500", description = "内部服务器错误")
            })
    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public DubboResult<Boolean> ping() {
        return DubboResult.buildSuccessResult(healthService.ping());
    }
}