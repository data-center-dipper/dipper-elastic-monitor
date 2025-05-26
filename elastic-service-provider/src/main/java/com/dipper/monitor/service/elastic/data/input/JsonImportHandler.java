package com.dipper.monitor.service.elastic.data.input;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.data.ImportDataReq;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JSON 文件导入处理器
 */
@Slf4j
public class JsonImportHandler extends AbstractImportHandler {

    private final AtomicInteger linesProcessed = new AtomicInteger(0);

    /**
     * 构造函数
     */
    public JsonImportHandler(ImportDataReq importDataReq, int totalLines, ProgressUpdater progressUpdater) {
        super(importDataReq, totalLines, progressUpdater);
    }

    /**
     * 处理单行 JSON 数据
     *
     * @param line JSON 字符串
     * @return 处理是否成功
     */
    @Override
    public boolean processLine(String line) {
        try {
            // 解析JSON
            JSONObject jsonObject = JSON.parseObject(line);

            // 写入 Elasticsearch
            writeToElasticsearch(jsonObject);
            return true;
        } catch (Exception e) {
            return handleError("JSON 解析失败: " + line, e);
        }
    }

    /**
     * 写入 Elasticsearch
     * @param document 要写入的文档
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
            while ((line = reader.readLine()) != null) {
                // 处理行，如果处理失败且不忽略错误，则中断处理
                if (!processLine(line) && !ignoreErrors) {
                    progressUpdater.updateProgress(0, "JSON 导入失败: 处理数据时发生错误", errorCount.get());
                    return;
                }
                
                // 更新进度
                int currentProgress = calculateProgress(linesProcessed.incrementAndGet(), totalLines);
                progressUpdater.updateProgress(currentProgress, 
                        "正在处理第 " + linesProcessed.get() + " 行...", 
                        errorCount.get());
            }
            
            // 完成导入
            progressUpdater.updateProgress(100, "JSON 导入完成" + 
                    (errorCount.get() > 0 ? "，但有 " + errorCount.get() + " 条数据处理失败" : ""), 
                    errorCount.get());
        } catch (Exception e) {
            log.error("处理 JSON 文件失败: {}", filePath, e);
            progressUpdater.updateProgress(0, "JSON 导入失败: " + e.getMessage(), errorCount.get());
        }
    }
}