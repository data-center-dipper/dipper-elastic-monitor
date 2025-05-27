package com.dipper.monitor.controller.elastic.slowsearch;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.slowsearch.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryView;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillQueryReq;
import com.dipper.monitor.service.elastic.slowsearch.SlowSearchKillService;
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
            boolean result = slowSearchKillService.killQuery(killQueryReq);
            return ResultUtils.onSuccess(result);
        } catch (Exception e) {
            log.error("终止查询失败", e);
            return ResultUtils.onFail(500, "终止查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/killPage")
    @Operation(summary = "分页查询任务杀死列表", description = "分页查询任务杀死列表")
    public JSONObject killPage(@RequestBody KillPageReq killPageReq) {
        try {
            Tuple2<List<KillTimeoutRecord>, Long> pageResult = slowSearchKillService.killPage(killPageReq);
            List<KillTimeoutRecord> records = pageResult.getK();
            Long total = pageResult.getV();
            return ResultUtils.onSuccessWithPageTotal(total.intValue(), records);
        } catch (Exception e) {
            log.error("分页查询终止记录失败", e);
            return ResultUtils.onFail(500, "分页查询终止记录失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/killDetail/{recordId}")
    @Operation(summary = "获取终止记录详情", description = "获取终止记录详情")
    public JSONObject killDetail(@PathVariable Integer recordId) {
        try {
            KillTimeoutRecord record = slowSearchKillService.getKillRecordDetail(recordId);
            if (record == null) {
                return ResultUtils.onFail(404, "未找到终止记录");
            }
            return ResultUtils.onSuccess(record);
        } catch (Exception e) {
            log.error("获取终止记录详情失败", e);
            return ResultUtils.onFail(500, "获取终止记录详情失败: " + e.getMessage());
        }
    }
}
