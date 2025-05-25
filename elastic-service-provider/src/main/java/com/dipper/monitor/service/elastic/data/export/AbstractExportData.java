package com.dipper.monitor.service.elastic.data.export;

import com.dipper.monitor.entity.elastic.data.ExportDataReq;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.data.DataExportService;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractExportData implements Runnable {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractExportData.class);
    protected final String taskId;
    protected final ExportDataReq exportDataReq;
    protected final ElasticClientService elasticClientService;
    protected final DataExportService dataExportService;

    public AbstractExportData(String taskId, ExportDataReq exportDataReq,
                              ElasticClientService elasticClientService,
                              DataExportService dataExportService) {
        this.taskId = taskId;
        this.exportDataReq = exportDataReq;
        this.elasticClientService = elasticClientService;
        this.dataExportService = dataExportService;
    }

    @Override
    public void run() {
        try {
            // 初始化任务进度
            ProgressInfo progressInfo = dataExportService.getExportTasks().get(taskId);
            if (progressInfo == null) return;

            progressInfo.setProgress(0);
            progressInfo.setStatus("processing");
            progressInfo.setFilePath("");

            // 构建查询API路径
            String api = "/" + exportDataReq.getIndex() + "/_search";

            // 构造请求体
            String requestBody = buildSearchRequestBody(exportDataReq.getQuery(), exportDataReq.getSize());
            HttpEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);

            // 发送 POST 请求并获取 Response 对象
            String response = elasticClientService.executePostApi(api, entity);

            // 解析响应中的 hits 数据
            List<Map<String, Object>> hits = parseHitsFromResponse(response); // 需要你自己实现

            // 创建导出文件路径
            String fileName = "export_" + exportDataReq.getIndex() + "_" + System.currentTimeMillis() + getExtension();
            String filePath = System.getProperty("java.io.tmpdir") + File.separator + fileName;

            // 导出数据到目标格式（JSON / CSV）
            exportData(hits, filePath, progressInfo);

            // 更新进度为完成
            progressInfo.setProgress(100);
            progressInfo.setStatus("completed");
            progressInfo.setFilePath(filePath);

        } catch (Exception e) {
            logger.error("导出数据失败", e);
            ProgressInfo progressInfo = dataExportService.getExportTasks().get(taskId);
            if (progressInfo != null) {
                progressInfo.setStatus("failed");
                progressInfo.setError(e.getMessage());
            }
        }
    }

    protected abstract String getExtension();

    protected abstract void exportData(List<Map<String, Object>> hits, String filePath, ProgressInfo progressInfo) throws IOException;

    private String buildSearchRequestBody(String query, int size) {
        // 如果 query 为空，默认是 match_all
        if (query == null || query.trim().isEmpty()) {
            return "{\"size\":" + size + ",\"query\":{\"match_all\":{}}}";
        }
        return "{\"size\":" + size + ",\"query\":" + query + "}";
    }

    // 解析响应中的 hits 数据
    protected List<Map<String, Object>> parseHitsFromResponse(String responseJson) {
        // 实际应使用 JSON 解析库（如 Jackson/Gson）解析 responseJson 中的 hits 字段
        // 这里简化处理，返回一个空列表作为示例
        return new ArrayList<>();
    }

    protected void updateProgress(ProgressInfo progressInfo, int total, int current) {
        if (total <= 0) return;
        int progress = (int) (((double) current / total) * 100);
        progressInfo.setProgress(progress);
        logger.debug("导出进度: {}%", progress);
    }
}