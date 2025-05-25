package com.dipper.monitor.service.elastic.data.input;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.data.ImportDataReq;
import com.dipper.monitor.vo.ProgressInfo;
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
    public JsonImportHandler(ImportDataReq importDataReq) throws Exception {
        super(importDataReq);
    }



    /**
     * 处理单行 JSON 数据
     *
     * @param line JSON 字符串
     */
    @Override
    public void processLine(String line) {
        try {
            JSONObject jsonObject = JSON.parseObject(line);

            // 模拟写入 Elasticsearch
            writeToElasticsearch(jsonObject);

        } catch (Exception e) {
            log.error("JSON 解析失败: {}", line, e);
        }
    }

    /**
     * 模拟写入 Elasticsearch
     * 实际项目中应替换为真实调用 Elasticsearch 客户端的代码
     *
     * @param document 要写入的文档
     */
    private void writeToElasticsearch(JSONObject document) {
        // TODO: 替换为实际 ES 写入逻辑，如：
        // elasticsearchTemplate.convertAndSend(...)

        // 当前只是打印日志
        log.info("写入 Elasticsearch 数据: {}", document.toJSONString());
    }

    /**
     * 计算当前进度百分比
     */
    private int calculateProgress(int current, int total) {
        if (total <= 0) return 0;
        return Math.min(95, (int) (((double) current / total) * 95));
    }


    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line);
                int currentProgress = calculateProgress(linesProcessed.incrementAndGet(), totalLines);
                progressUpdater.updateProgress(currentProgress, "正在处理第 " + linesProcessed.get() + " 行...");
            }
            progressUpdater.updateProgress(100, "JSON 导入完成");
        } catch (Exception e) {
            log.error("处理 JSON 文件失败: {}", filePath, e);
            progressUpdater.updateProgress(0, "JSON 导入失败: " + e.getMessage());
        }
    }
}