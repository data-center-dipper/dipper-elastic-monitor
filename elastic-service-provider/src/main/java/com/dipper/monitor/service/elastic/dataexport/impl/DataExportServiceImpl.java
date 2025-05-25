package com.dipper.monitor.service.elastic.dataexport.impl;

import com.dipper.monitor.entity.elastic.data.ExportDataReq;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.dataexport.DataExportService;
import com.dipper.monitor.service.elastic.dataexport.handlers.CsvExportDataHandler;
import com.dipper.monitor.service.elastic.dataexport.handlers.JsonExportDataHandler;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DataExportServiceImpl implements DataExportService {

    private static final Logger logger = LoggerFactory.getLogger(DataExportServiceImpl.class);

    // 存储导出任务的进度信息，Key: taskId, Value: ProgressInfo
    private final Map<String, ProgressInfo> exportTasks = new ConcurrentHashMap<>();

    // 固定大小线程池用于处理导出任务
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticRealIndexService elasticRealIndexService;

    @Override
    public String exportData(ExportDataReq exportDataReq) {
        // 生成唯一的任务ID
        String taskId = java.util.UUID.randomUUID().toString();

        // 验证格式是否合法
        String format = exportDataReq.getFormat();
        if (format == null || (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("csv"))) {
            throw new IllegalArgumentException("不支持的导出格式: " + format);
        }

        // 初始化任务进度信息
        ProgressInfo progressInfo = new ProgressInfo();
        progressInfo.setProgress(0);
        progressInfo.setStatus("processing");
        progressInfo.setFilePath(null);
        progressInfo.setError(null);

        exportTasks.put(taskId, progressInfo);

        // 启动异步任务
        if ("json".equalsIgnoreCase(format)) {
            executorService.submit(new JsonExportDataHandler(taskId, exportDataReq,
                    elasticClientService,this));
        } else {
            executorService.submit(new CsvExportDataHandler(taskId, exportDataReq,
                    elasticClientService,this));
        }

        logger.info("已启动导出任务，任务ID: {}", taskId);
        return taskId;
    }

    @Override
    public ProgressInfo getExportProgress(String taskId) {
        return exportTasks.getOrDefault(taskId, new ProgressInfo("not_found", 0, "任务不存在"));
    }

    /**
     * 获取所有当前任务（可选）
     */
    public Map<String, ProgressInfo> getAllTasks() {
        return new HashMap<>(exportTasks);
    }

    /**
     * 清理已完成或失败的任务（可定期调用）
     */
    public void cleanupCompletedTasks() {
        exportTasks.forEach((taskId, info) -> {
            if ("completed".equals(info.getStatus()) || "failed".equals(info.getStatus())) {
                exportTasks.remove(taskId);
                logger.info("已清理任务: {}", taskId);
            }
        });
    }

    // 提供对 exportTasks 的访问方法，供 Handler 使用
    public Map<String, ProgressInfo> getExportTasks() {
        return exportTasks;
    }
}