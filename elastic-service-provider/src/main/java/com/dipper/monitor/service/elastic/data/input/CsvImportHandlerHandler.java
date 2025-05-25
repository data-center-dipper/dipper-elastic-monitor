package com.dipper.monitor.service.elastic.data.input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvImportHandlerHandler extends AbstractImportHandler {

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
}