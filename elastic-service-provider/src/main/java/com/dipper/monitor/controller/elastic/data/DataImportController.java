package com.dipper.monitor.controller.elastic.data;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.data.ExportDataReq;
import com.dipper.monitor.entity.elastic.data.ImportDataReq;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import com.dipper.monitor.service.elastic.data.DataImportService;
import com.dipper.monitor.utils.ResultUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/data_import")
public class DataImportController {

    @Autowired
    private DataImportService dataImportService;

    /**
     * 开始导入数据
     */
    @PostMapping("/importData")
    public JSONObject importData(@RequestBody ImportDataReq importDataReq) {
        try {
            String taskId  = dataImportService.importData(importDataReq);
            return ResultUtils.onSuccess(taskId);
        } catch (IllegalArgumentException e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }
    
    /**
     * 获取导入进度
     */
    @GetMapping("/progress/{taskId}")
    public JSONObject getImportProgress(@PathVariable String taskId) {
        try {
            ProgressInfo progress = dataImportService.getImportProgress(taskId);
            return ResultUtils.onSuccess(progress);
        } catch (IllegalArgumentException e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }
    

}
