package com.dipper.monitor.service.elastic.data.impl;

import com.dipper.monitor.entity.elastic.data.ImportDataReq;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import com.dipper.monitor.service.elastic.data.DataImportService;
import com.dipper.monitor.service.elastic.data.input.CsvImportHandlerHandler;
import com.dipper.monitor.service.elastic.data.input.ImportTask;
import com.dipper.monitor.service.elastic.data.input.JsonImportHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class DataImportServiceImpl implements DataImportService {

    private final Map<String, Runnable> taskMap = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public String importData(ImportDataReq importDataReq) {
        String format = importDataReq.getFormat();
        Runnable runnable = null;
        if ("csv".equalsIgnoreCase(format)) {
            runnable = new JsonImportHandler(importDataReq);
        }else if ("json".equalsIgnoreCase(format)) {
            runnable = new CsvImportHandlerHandler(importDataReq);
        }
        String taskId = "import_" + System.currentTimeMillis();


        taskMap.put(taskId, runnable);
        log.info("开始执行导入任务: {}", taskId);

        executor.submit(runnable);

        return taskId;
    }

    @Override
    public ProgressInfo getImportProgress(String taskId) {
        Runnable task = taskMap.get(taskId);
        if (task == null) {
            return new ProgressInfo("not_found", 0, "任务不存在");
        }

        return task.getProgressInfo();
    }
}