package com.dipper.monitor.service.elastic.data.input;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.data.ImportDataReq;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CSV 文件导入处理器
 */
@Slf4j
public class CsvImportHandler extends AbstractImportHandler {

    private final AtomicInteger linesProcessed = new AtomicInteger(0);
    private List<String> headers = new ArrayList<>();

    /**
     * 构造函数
     */
    public CsvImportHandler(ImportDataReq importDataReq, int totalLines, ProgressUpdater progressUpdater) {
        super(importDataReq, totalLines, progressUpdater);
    }

    /**
     * 处理单行 CSV 数据
     *
     * @param line CSV 字符串
     * @return 处理是否成功
     */
    @Override
    public boolean processLine(String line) {
        try {
            // 如果是第一行且headers为空，则解析为表头
            if (headers.isEmpty()) {
                parseHeaders(line);
                return true;
            }

            // 解析CSV行为JSON对象
            JSONObject jsonObject = parseCSVLine(line);

            // 写入 Elasticsearch
            writeToElasticsearch(jsonObject);
            return true;
        } catch (Exception e) {
            return handleError("CSV 解析失败: " + line, e);
        }
    }

    /**
     * 解析CSV表头
     */
    private void parseHeaders(String headerLine) {
        String[] headerArray = headerLine.split(",");
        headers = new ArrayList<>(headerArray.length);
        for (String header : headerArray) {
            headers.add(header.trim());
        }
        log.info("CSV表头解析完成: {}", headers);
    }

    /**
     * 解析CSV行为JSON对象
     */
    private JSONObject parseCSVLine(String line) {
        String[] values = line.split(",");
        JSONObject jsonObject = new JSONObject();

        for (int i = 0; i < Math.min(headers.size(), values.length); i++) {
            jsonObject.put(headers.get(i), values[i].trim());
        }

        return jsonObject;
    }

    /**
     * 写入 Elasticsearch
     */
    private void writeToElasticsearch(JSONObject document) {
        // TODO: 替换为实际 ES 写入逻辑，如：
        // elasticsearchTemplate.convertAndSend(importDataReq.getIndex(), document);

        // 当前只是打印日志
        log.info("写入 Elasticsearch 索引 {}: {}", importDataReq.getIndex(), document.toJSONString());
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // 处理行
                boolean success = processLine(line);
                
                // 如果是第一行（表头），不计入处理行数
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // 如果处理失败且不忽略错误，则中断处理
                if (!success && !ignoreErrors) {
                    progressUpdater.updateProgress(0, "CSV 导入失败: 处理数据时发生错误", errorCount.get());
                    return;
                }
                
                // 更新进度
                int currentProgress = calculateProgress(linesProcessed.incrementAndGet(), totalLines - 1); // 减1是因为表头不计入
                progressUpdater.updateProgress(currentProgress, 
                        "正在处理第 " + linesProcessed.get() + " 行...", 
                        errorCount.get());
            }
            
            // 完成导入
            progressUpdater.updateProgress(100, "CSV 导入完成" + 
                    (errorCount.get() > 0 ? "，但有 " + errorCount.get() + " 条数据处理失败" : ""), 
                    errorCount.get());
        } catch (Exception e) {
            log.error("处理 CSV 文件失败: {}", filePath, e);
            progressUpdater.updateProgress(0, "CSV 导入失败: " + e.getMessage(), errorCount.get());
        }
    }
}