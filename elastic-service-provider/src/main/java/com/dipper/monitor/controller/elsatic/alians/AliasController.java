package com.dipper.monitor.controller.elsatic.alians;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.alians.AliasListView;
import com.dipper.monitor.entity.elastic.alians.AliasPageReq;
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
            List<IndexAliasRelation> pageResult = elasticAliasService.aliasCheck();
            return  ResultUtils.onSuccess(pageResult);
        } catch (Exception e) {
            log.error("分页查询失败", e);
            return ResultUtils.onFail(500, "分页查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/aliasRepair")
    @Operation(summary = "别名冲突修复", description = "别名冲突修复")
    public JSONObject aliasRepair() {
        try {
            List<String> pageResult = elasticAliasService.aliasRepair();
            return  ResultUtils.onSuccess(pageResult);
        } catch (Exception e) {
            log.error("分页查询失败", e);
            return ResultUtils.onFail(500, "分页查询失败: " + e.getMessage());
        }
    }
}
