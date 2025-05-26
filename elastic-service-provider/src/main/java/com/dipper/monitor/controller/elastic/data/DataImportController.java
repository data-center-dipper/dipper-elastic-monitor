package com.dipper.monitor.controller.elastic.data;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.data.ImportDataReq;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import com.dipper.monitor.service.elastic.data.DataImportService;
import com.dipper.monitor.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public JSONObject importData(@ModelAttribute ImportDataReq importDataReq) {
        try {
            // 直接调用服务层处理导入请求
            String taskId = dataImportService.importData(importDataReq);
            return ResultUtils.onSuccess(taskId);
        } catch (IllegalArgumentException e) {
            log.error("导入参数错误", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("导入操作失败", e);
            return ResultUtils.onFail("导入操作失败: " + e.getMessage());
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
            log.error("获取进度参数错误", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("获取进度失败", e);
            return ResultUtils.onFail("获取进度失败: " + e.getMessage());
        }
    }
}
