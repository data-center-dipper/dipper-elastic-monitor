package com.dipper.monitor.service.elastic.data.input;

import com.dipper.monitor.entity.elastic.data.ImportDataReq;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ImportTask implements Runnable {

    private final String taskId;
    private final ImportDataReq req;
    private final AtomicInteger progress = new AtomicInteger(0);
    private volatile ProgressInfo progressInfo;
    private ImportHandler handler;

    public ImportTask(String taskId, ImportDataReq req) {
        this.taskId = taskId;
        this.req = req;
        this.progressInfo = new ProgressInfo("pending", 0, "任务准备中...", 0);
    }

    @Override
    public void run() {
        try {
            updateProgress(10, "开始导入...", 0);

            // 计算文件总行数
            int totalLines = countLines(req.getFilePath());
            log.info("文件总行数: {}", totalLines);

            // 创建处理器
            createHandler(totalLines);

            // 启动处理器
            handler.run();

            log.info("任务 {} 执行完成", taskId);
        } catch (Exception e) {
            log.error("任务 {} 执行失败", taskId, e);
            updateProgress(0, "导入失败：" + e.getMessage(), 0);
        } finally {
            // 清理临时文件
            cleanupTempFile();
        }
    }

    /**
     * 创建适合的处理器
     */
    private void createHandler(int totalLines) {
        ProgressUpdater updater = this::updateProgress;

        if ("json".equalsIgnoreCase(req.getFormat())) {
            handler = new JsonImportHandler(req, totalLines, updater);
        } else if ("csv".equalsIgnoreCase(req.getFormat())) {
            handler = new CsvImportHandler(req, totalLines, updater);
        } else {
            throw new IllegalArgumentException("不支持的文件格式: " + req.getFormat());
        }
    }

    /**
     * 计算文件行数
     */
    private int countLines(String filePath) throws IOException {
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while (reader.readLine() != null) lines++;
        }
        return lines;
    }

    /**
     * 更新进度信息
     */
    private synchronized void updateProgress(int newProgress, String message, int errorCount) {
        progress.set(newProgress);
        progressInfo = new ProgressInfo(
                newProgress == 100 ? "completed" : (newProgress == 0 && message.contains("失败") ? "failed" : "running"),
                newProgress,
                message,
                errorCount
        );
    }

    /**
     * 清理临时文件
     */
    private void cleanupTempFile() {
        try {
            if (req.getFilePath() != null) {
                java.io.File tempFile = new java.io.File(req.getFilePath());
                if (tempFile.exists() && tempFile.isFile()) {
                    boolean deleted = tempFile.delete();
                    if (deleted) {
                        log.info("临时文件已删除: {}", req.getFilePath());
                    } else {
                        log.warn("临时文件删除失败: {}", req.getFilePath());
                    }
                }
            }
        } catch (Exception e) {
            log.error("清理临时文件失败", e);
        }
    }

    /**
     * 获取进度信息
     */
    public ProgressInfo getProgressInfo() {
        return progressInfo;
    }
}