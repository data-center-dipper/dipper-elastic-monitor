package com.dipper.monitor.controller.elastic.dataexport;

import com.dipper.monitor.service.elastic.dataexport.DataExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/elastic/export")
public class DataExportController {

    @Autowired
    private DataExportService dataExportService;
    
    /**
     * 搜索索引
     */
    @GetMapping("/indices")
    public ResponseEntity<List<String>> searchIndices(@RequestParam(required = false) String keyword) {
        List<String> indices = dataExportService.searchIndices(keyword);
        return ResponseEntity.ok(indices);
    }
    
    /**
     * 开始导出数据
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startExport(@RequestBody Map<String, Object> params) {
        String index = (String) params.get("index");
        String query = (String) params.get("query");
        String format = (String) params.get("format");
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 1000;
        
        String taskId = dataExportService.exportData(index, query, format, size);
        
        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取导出进度
     */
    @GetMapping("/progress/{taskId}")
    public ResponseEntity<Map<String, Object>> getExportProgress(@PathVariable String taskId) {
        Map<String, Object> progress = dataExportService.getExportProgress(taskId);
        return ResponseEntity.ok(progress);
    }
    
    /**
     * 下载导出文件
     * 注意：实际实现中应该添加文件下载逻辑
     */
    @GetMapping("/download/{taskId}")
    public ResponseEntity<Map<String, String>> downloadExportFile(@PathVariable String taskId) {
        Map<String, Object> progress = dataExportService.getExportProgress(taskId);
        
        if ("completed".equals(progress.get("status"))) {
            Map<String, String> response = new HashMap<>();
            response.put("filePath", (String) progress.get("filePath"));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
