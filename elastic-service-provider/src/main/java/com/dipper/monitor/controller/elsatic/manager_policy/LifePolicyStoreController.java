package com.dipper.monitor.controller.elsatic.manager_policy;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.dto.request.LifePolicyRequest;
import com.dipper.monitor.dto.request.PolicyPageRequest;
import com.dipper.monitor.dto.response.LifePolicyResponse;
import com.dipper.monitor.service.elastic.policy.LifePolicyStoreService;
import com.dipper.monitor.utils.ResultUtils;
import com.dipper.monitor.utils.Tuple2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/policy")
@Tag(name = "ES策略管理", description = "管理和维护Elasticsearch生命周期策略")
public class LifePolicyStoreController {

    @Autowired
    private LifePolicyStoreService lifePolicyStoreService;

    @PostMapping("/add")
    @Operation(summary = "添加策略", description = "添加新的生命周期策略")
    public JSONObject addPolicy(@RequestBody LifePolicyRequest request) {
        try {
            LifePolicyResponse response = lifePolicyStoreService.addPolicy(request);
            return ResultUtils.onSuccess(response);
        } catch (IllegalArgumentException e) {
            log.warn("添加策略参数错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("添加策略失败", e);
            return ResultUtils.onFail(500, "添加策略失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    @Operation(summary = "更新策略", description = "更新现有的生命周期策略")
    public JSONObject updatePolicy(@RequestBody LifePolicyRequest request) {
        try {
            LifePolicyResponse response = lifePolicyStoreService.updatePolicy(request);
            return  ResultUtils.onSuccess(response);
        } catch (IllegalArgumentException e) {
            log.warn("更新策略参数错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新策略失败", e);
            return ResultUtils.onFail(500, "更新策略失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除策略", description = "根据ID删除生命周期策略")
    public JSONObject deletePolicy(@PathVariable("id") Integer id) {
        try {
            boolean success = lifePolicyStoreService.deletePolicy(id);
            return  ResultUtils.onSuccess(success);
        } catch (IllegalArgumentException e) {
            log.warn("删除策略参数错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("删除策略失败", e);
            return ResultUtils.onFail(500, "删除策略失败: " + e.getMessage());
        }
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取策略详情", description = "根据ID获取生命周期策略详情")
    public JSONObject getOnePolicy(@PathVariable("id") Integer id) {
        try {
            LifePolicyResponse response = lifePolicyStoreService.getOnePolicy(id);
            if (response == null) {
                return ResultUtils.onFail(404, "策略不存在");
            }
            return  ResultUtils.onSuccess(response);
        } catch (IllegalArgumentException e) {
            log.warn("获取策略参数错误: {}", e.getMessage());
            return ResultUtils.onFail(400, e.getMessage());
        } catch (Exception e) {
            log.error("获取策略失败", e);
            return ResultUtils.onFail(500, "获取策略失败: " + e.getMessage());
        }
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询策略", description = "分页查询生命周期策略列表")
    public JSONObject getPoliciesByPage(@RequestBody PolicyPageRequest request) {
        try {
            Tuple2<List<LifePolicyResponse>,Long> pageResult = lifePolicyStoreService.getPoliciesByPage(request);
            List<LifePolicyResponse> k = pageResult.getK();
            Long v = pageResult.getV();
            return  ResultUtils.onSuccessWithPageTotal(v,k);
        } catch (Exception e) {
            log.error("分页查询策略失败", e);
            return ResultUtils.onFail(500, "分页查询策略失败: " + e.getMessage());
        }
    }
}
