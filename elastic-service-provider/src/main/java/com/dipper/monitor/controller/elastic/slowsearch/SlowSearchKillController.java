package com.dipper.monitor.controller.elastic.slowsearch;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryView;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillQueryReq;
import com.dipper.monitor.service.elastic.slowsearch.SlowSearchKillService;
import com.dipper.monitor.service.elastic.slowsearch.SlowSearchService;
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
@RequestMapping("/dipper/monitor/api/v1/elastic/slow_search")
@Tag(name = "慢查询相关", description = "慢查询相关")
public class SlowSearchKillController {

    @Autowired
    private SlowSearchKillService slowSearchKillService;

    @PostMapping("/killQuery")
    @Operation(summary = "杀死慢查询", description = "杀死慢查询")
    public JSONObject killQuery(@RequestBody KillQueryReq killQueryReq) {
        try {
            boolean pageResult = slowSearchKillService.killQuery(killQueryReq);
            return  ResultUtils.onSuccess(pageResult);
        } catch (Exception e) {
            log.error("分页查询策略失败", e);
            return ResultUtils.onFail(500, "分页查询策略失败: " + e.getMessage());
        }
    }

    @PostMapping("/slowSearchPage")
    @Operation(summary = "分页查询慢查询", description = "分页查询慢查询")
    public JSONObject slowSearchPage(@RequestBody SlowQueryPageReq pageReq) {
        try {
            Tuple2<List<SlowQueryView>,Integer> pageResult = slowSearchService.slowSearchPage(pageReq);
            List<SlowQueryView> k = pageResult.getK();
            Integer v = pageResult.getV();
            return  ResultUtils.onSuccessWithPageTotal(v,k);
        } catch (Exception e) {
            log.error("分页查询策略失败", e);
            return ResultUtils.onFail(500, "分页查询策略失败: " + e.getMessage());
        }
    }

}
