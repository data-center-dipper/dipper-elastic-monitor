package com.dipper.monitor.service.elastic.data.export;

import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import com.dipper.monitor.entity.elastic.data.ExportDataReq;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.data.DataExportService;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonExportDataHandler extends AbstractExportData {

    public JsonExportDataHandler(String taskId, ExportDataReq exportDataReq,
                                 ElasticClientService elasticClientService,
                                 DataExportService dataExportService) {
        super(taskId, exportDataReq, elasticClientService,dataExportService);
    }

    @Override
    protected String getExtension() {
        return ".json";
    }

    @Override
    protected void exportData(List<Map<String, Object>> hits, String filePath, ProgressInfo progressInfo) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("[\n");

            for (int i = 0; i < hits.size(); i++) {
                Map<String, Object> hit = hits.get(i);
                writer.write(objectToJson(hit));

                if (i < hits.size() - 1) {
                    writer.write(",\n");
                } else {
                    writer.write("\n");
                }

                updateProgress(progressInfo, hits.size(), i + 1);
            }

            writer.write("]");
        }
    }

    private String objectToJson(Map<String, Object> obj) {
        // 简化版转换，实际应用中建议使用Jackson或Gson等库
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                sb.append("\"").append(entry.getValue()).append("\"");
            } else {
                sb.append(entry.getValue());
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}