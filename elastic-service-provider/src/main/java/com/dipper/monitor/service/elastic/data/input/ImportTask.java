package com.dipper.monitor.service.elastic.data.input;

import com.dipper.monitor.entity.elastic.data.ImportDataReq;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class ImportTask implements Runnable {

    private final String taskId;
    private final ImportDataReq req;
    private final AtomicInteger progress = new AtomicInteger(0);
    private volatile ProgressInfo progressInfo;
    private final AbstractImportHandler handler;

    @Autowired
    public ImportTask(String taskId, ImportDataReq req) {
        this.taskId = taskId;
        this.req = req;
        this.handler = "json".equalsIgnoreCase(req.getFormat()) ?
                new JsonImportHandler() : new CsvImportHandlerHandler();
        this.progressInfo = new ProgressInfo("pending", 0, "任务准备中...");
    }

    @Override
    public void run() {
        try {
            updateProgress(10, "开始导入...");

            // 模拟文件读取和解析过程
            readFileAndProcess();

            updateProgress(100, "导入完成");
            log.info("任务 {} 成功完成", taskId);
        } catch (Exception e) {
            log.error("任务 {} 执行失败", taskId, e);
            updateProgress(0, "导入失败：" + e.getMessage());
        }
    }

    private void readFileAndProcess() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(req.getFilePath()))) {
            String line;
            int totalLines = countLines(req.getFilePath());
            int currentLine = 0;

            while ((line = reader.readLine()) != null) {
                currentLine++;
                processLine(line);

                // 更新进度
                int newProgress = Math.min(95, (int)(((double)currentLine / totalLines) * 90));
                if (newProgress > progress.get()) {
                    updateProgress(newProgress, "正在处理第 " + currentLine + " 行...");
                }
            }
        }
    }

    private void processLine(String line) {
        // 根据格式调用不同的处理器
        handler.processLine(line);
    }

    private int countLines(String filePath) throws IOException {
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while (reader.readLine() != null) lines++;
        }
        return lines;
    }

    private synchronized void updateProgress(int newProgress, String message) {
        progress.set(newProgress);
        progressInfo = new ProgressInfo(
                newProgress == 100 ? "completed" : "running",
                newProgress,
                message
        );
    }

    public ProgressInfo getProgressInfo() {
        return progressInfo;
    }

    // Handler接口定义
    interface LineProcessor {
        void processLine(String line);
    }



}