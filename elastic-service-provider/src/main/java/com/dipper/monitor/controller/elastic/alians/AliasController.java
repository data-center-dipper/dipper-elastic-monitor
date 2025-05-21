package com.dipper.monitor.controller.elastic.alians;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.alians.AliasListView;
import com.dipper.monitor.entity.elastic.alians.AliasPageReq;
import com.dipper.monitor.entity.elastic.alians.AliasRepairInfo;
import com.dipper.monitor.entity.elastic.alians.IndexAliasRelation;
import com.dipper.monitor.service.elastic.alians.ElasticAliasService;
import com.dipper.monitor.utils.ResultUtils;
import com.dipper.monitor.utils.Tuple2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/alias_manager")
@Tag(name = "别名管理", description = "别名管理")
public class AliasController {

    @Autowired
    private ElasticAliasService elasticAliasService;

    @PostMapping("/aliasPage")
    @Operation(summary = "分页查询", description = "分页查询")
    public JSONObject getAliasByPage(@RequestBody AliasPageReq aliasPageReq) {
        try {
            Tuple2<List<AliasListView>,Long> pageResult = elasticAliasService.getAliasByPage(aliasPageReq);
            List<AliasListView> k = pageResult.getK();
            Long v = pageResult.getV();
            return  ResultUtils.onSuccessWithPageTotal(v,k);
        } catch (Exception e) {
            log.error("分页查询失败", e);
            return ResultUtils.onFail(500, "分页查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/aliasCheck")
    @Operation(summary = "别名冲突检测", description = "别名冲突检测")
    public JSONObject aliasCheck() {
        try {
            Map<String, List<IndexAliasRelation>> pageResult = elasticAliasService.aliasCheck();
            return  ResultUtils.onSuccess(pageResult);
        } catch (Exception e) {
            log.error("分页查询失败", e);
            return ResultUtils.onFail(500, "分页查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/aliasRepair")
    @Operation(summary = "别名冲突修复", description = "根据提供的别名信息修复冲突")
    public JSONObject aliasRepair(@RequestBody AliasRepairInfo aliasRepairInfo) {
        try {
            elasticAliasService.aliasAutoRepair(aliasRepairInfo);
            return  ResultUtils.onSuccess();
        } catch (Exception e) {
            log.error("别名修复失败", e);
            return  ResultUtils.onFail();
        }
    }

    @GetMapping("/aliasAutoRepair")
    @Operation(summary = "别名冲突自动修复", description = "别名冲突自动修复")
    public JSONObject aliasAutoRepair() {
        try {
            List<String> pageResult = elasticAliasService.aliasAutoRepair();
            return  ResultUtils.onSuccess(pageResult);
        } catch (Exception e) {
            log.error("别名冲突自动修复失败", e);
            return ResultUtils.onFail(500, "别名冲突自动修复失败: " + e.getMessage());
        }
    }
}
