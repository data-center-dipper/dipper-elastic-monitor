package com.dipper.monitor.controller.elastic.policy;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.service.elastic.policy.LifePolicyRealService;
import com.dipper.monitor.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/real_policy")
@Tag(name = "ES集群真正运行的策略管理", description = "ES集群真正运行的策略管理")
public class LifePolicyRealController {

    @Autowired
    private LifePolicyRealService lifePolicyRealService;

    @PutMapping("/policyEffective/{id}")
    @Operation(summary = "立即生效", description = "让策略立即生效")
    public JSONObject policyEffective(@PathVariable("id") Integer id) {
        try {
            lifePolicyRealService.policyEffective(id);
            return  ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.warn("获取策略参数错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("获取策略失败", e);
            return ResultUtils.onFail(500, "获取策略失败: " + e.getMessage());
        }
    }


    @GetMapping("/policyList")
    @Operation(summary = "查看已经生效的策略", description = "查看已经生效的策略")
    public JSONObject policyList() {
        try {
            lifePolicyRealService.policyList();
            return  ResultUtils.onSuccess();
        } catch (IllegalArgumentException e) {
            log.warn("获取策略参数错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("获取策略失败", e);
            return ResultUtils.onFail(500, "获取策略失败: " + e.getMessage());
        }
    }


}
