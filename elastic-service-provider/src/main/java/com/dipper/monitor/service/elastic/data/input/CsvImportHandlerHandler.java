package com.dipper.monitor.service.elastic.data.input;

import com.dipper.monitor.entity.elastic.data.ImportDataReq;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvImportHandlerHandler extends AbstractImportHandler {

    /**
     * 构造函数
     *
     * @param importDataReq   导入请求参数
     * @param totalLines      总行数
     * @param progressUpdater 进度更新器
     */
    public CsvImportHandlerHandler(ImportDataReq importDataReq, int totalLines, ProgressUpdater progressUpdater) {
        super(importDataReq, totalLines, progressUpdater);
    }

    @Override
    public void run() {
        // 示例：读取文件并处理 CSV 数据
        try (BufferedReader reader = new BufferedReader(new FileReader("your_file.csv"))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // 跳过表头
                }
                // 解析 CSV 并插入 ES
                System.out.println("导入 CSV 数据: " + line);
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV 导入失败", e);
        }
    }

    @Override
    public boolean processLine(String line) {
        return false;
    }
}