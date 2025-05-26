package com.dipper.monitor.service.elastic.data.export;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.config.ExportConfig;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import com.dipper.monitor.entity.elastic.data.ExportDataReq;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.data.DataExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonExportDataHandler extends AbstractExportData {

    public JsonExportDataHandler(String taskId, ExportDataReq exportDataReq,
                                 ElasticClientService elasticClientService,
                                 DataExportService dataExportService,
                                 ExportConfig exportConfig) {
        super(taskId, exportDataReq, elasticClientService,dataExportService,exportConfig);
    }

    @Override
    protected String getExtension() {
        return ".json";
    }

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();


    @Override
    protected void exportData(List<JSONObject> hits, String filePath, ProgressInfo progressInfo) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (int i = 0; i < hits.size(); i++) {
                JSONObject hit = hits.get(i);

                // 使用 Jackson 将单个对象转成 JSON 字符串
                String jsonLine = objectMapper.writeValueAsString(hit);

                // 写入并换行
                writer.write(jsonLine);
                writer.write("\n");

                // 更新进度
                updateProgress(progressInfo, hits.size(), i + 1);
            }
        }
    }




}