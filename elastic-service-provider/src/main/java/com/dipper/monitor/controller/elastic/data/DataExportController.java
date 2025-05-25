package com.dipper.monitor.controller.elastic.data;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.data.ExportDataReq;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import com.dipper.monitor.service.elastic.data.DataExportService;
import com.dipper.monitor.utils.ResultUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/data_export")
public class DataExportController {

    @Autowired
    private DataExportService dataExportService;

    /**
     * 开始导出数据
     */
    @PostMapping("/exportData")
    public JSONObject exportData(@RequestBody ExportDataReq exportDataReq) {
        try {
            String taskId  = dataExportService.exportData(exportDataReq);
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
     * 获取导出进度
     */
    @GetMapping("/progress/{taskId}")
    public JSONObject getExportProgress(@PathVariable String taskId) {
        try {
            ProgressInfo progress = dataExportService.getExportProgress(taskId);
            return ResultUtils.onSuccess(progress);
        } catch (IllegalArgumentException e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail(e.getMessage());
        } catch (Exception e) {
            log.error("Error adding template", e);
            return ResultUtils.onFail("Operation error");
        }
    }
    
    /**
     * 下载导出文件
     * 注意：实际实现中应该添加文件下载逻辑
     */
    @GetMapping("/download/{taskId}")
    public void downloadExportFile(@PathVariable String taskId, HttpServletResponse response) {
        ProgressInfo progress = dataExportService.getExportProgress(taskId);

        if ("completed".equals(progress.getStatus())) {
            String filePath = (String) progress.getFilePath();
            File file = new File(filePath);

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

            try (InputStream is = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                log.error("文件下载失败", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
