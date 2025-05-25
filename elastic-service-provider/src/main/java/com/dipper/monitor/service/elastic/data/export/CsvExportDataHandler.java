package com.dipper.monitor.service.elastic.data.export;

import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import com.dipper.monitor.entity.elastic.data.ExportDataReq;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.data.DataExportService;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvExportDataHandler extends AbstractExportData {

    public CsvExportDataHandler(String taskId, ExportDataReq exportDataReq,
                                ElasticClientService elasticClientService,
                                DataExportService dataExportService ) {
        super(taskId, exportDataReq, elasticClientService,dataExportService);
    }

    @Override
    protected String getExtension() {
        return ".csv";
    }

    @Override
    protected void exportData(List<Map<String, Object>> hits, String filePath, ProgressInfo progressInfo) throws IOException {
        if (hits.isEmpty()) {
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(""); // 空文件
            }
            return;
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            // 获取第一条数据的所有字段作为CSV的表头
            Map<String, Object> firstHit = hits.get(0);
            List<String> headers = new ArrayList<>(firstHit.keySet());

            // 写入表头
            writer.write(String.join(",", escapeForCSV(headers)) + "\n");

            // 写入数据行
            for (int i = 0; i < hits.size(); i++) {
                Map<String, Object> hit = hits.get(i);
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    Object value = hit.get(header);
                    values.add(value != null ? escapeForCSV(value.toString()) : "");
                }
                writer.write(String.join(",", values) + "\n");

                updateProgress(progressInfo, hits.size(), i + 1);
            }
        }
    }

    /**
     * 转义CSV中的特殊字符，如逗号、双引号和换行符
     */
    private String escapeForCSV(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private List<String> escapeForCSV(List<String> fields) {
        return fields.stream()
                .map(this::escapeForCSV)
                .toList();
    }
}