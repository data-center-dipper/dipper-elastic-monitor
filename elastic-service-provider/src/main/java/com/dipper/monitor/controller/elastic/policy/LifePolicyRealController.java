package com.dipper.monitor.controller.elastic.policy;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.policy.PolicyPageRequest;
import com.dipper.monitor.entity.elastic.policy.response.LifePolicyResponse;
import com.dipper.monitor.service.elastic.policy.LifePolicyRealService;
import com.dipper.monitor.utils.ResultUtils;
import com.dipper.monitor.utils.Tuple2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


//    @GetMapping("/policyList")
//    @Operation(summary = "查看已经生效的策略", description = "查看已经生效的策略")
//    public JSONObject policyList() {
//        try {
//            lifePolicyRealService.policyList();
//            return  ResultUtils.onSuccess();
//        } catch (IllegalArgumentException e) {
//            log.warn("获取策略参数错误: {}", e.getMessage());
//            return ResultUtils.onFail(400, e.getMessage());
//        } catch (Exception e) {
//            log.error("获取策略失败", e);
//            return ResultUtils.onFail(500, "获取策略失败: " + e.getMessage());
//        }
//    }

    @PostMapping("/getRealPolicies")
    @Operation(summary = "查看已经生效的策略", description = "查看已经生效的策略")
    public JSONObject getRealPolicies(@RequestBody PolicyPageRequest request) {
        try {
            Tuple2<List<LifePolicyResponse>,Long> pageResult = lifePolicyRealService.getRealPolicies(request);
            List<LifePolicyResponse> k = pageResult.getK();
            Long v = pageResult.getV();
            return  ResultUtils.onSuccessWithPageTotal(v,k);
        } catch (Exception e) {
            log.error("分页查询策略失败", e);
            return ResultUtils.onFail(500, "分页查询策略失败: " + e.getMessage());
        }
    }


    @DeleteMapping("delete/{policyName}")
    @Operation(summary = "删除策略", description = "根据policyName删除生命周期策略")
    public JSONObject deletePolicy(@PathVariable("policyName") String policyName) {
        try {
            boolean success = lifePolicyRealService.deletePolicy(policyName);
            return  ResultUtils.onSuccess(success);
        } catch (IllegalArgumentException e) {
            log.warn("删除策略参数错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("删除策略失败", e);
            return ResultUtils.onFail(500, "删除策略失败: " + e.getMessage());
        }
    }

}
