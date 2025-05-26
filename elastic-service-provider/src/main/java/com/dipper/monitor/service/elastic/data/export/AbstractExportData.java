package com.dipper.monitor.service.elastic.data.export;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.config.ExportConfig;
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
    protected final ExportConfig exportConfig;

    public AbstractExportData(String taskId, ExportDataReq exportDataReq,
                              ElasticClientService elasticClientService,
                              DataExportService dataExportService,
                              ExportConfig exportConfig) {
        this.taskId = taskId;
        this.exportDataReq = exportDataReq;
        this.elasticClientService = elasticClientService;
        this.dataExportService = dataExportService;
        this.exportConfig = exportConfig;
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
            List<JSONObject> hits = parseHitsFromResponse(response); // 需要你自己实现

            // 创建导出文件路径
            String fileName = "export_" + exportDataReq.getIndex() + "_" + System.currentTimeMillis() + getExtension();
            String exportBasePath = exportConfig.getExportBasePath();
            if (exportBasePath == null || exportBasePath.isEmpty()) {
                throw new RuntimeException("Export base path is not configured");
            }
            String filePath = exportBasePath + File.separator + fileName;

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
                progressInfo.setMessage(e.getMessage()); // 使用setMessage替代setError
            }
        }
    }

    protected abstract String getExtension();

    protected abstract void exportData(List<JSONObject> hits, String filePath, ProgressInfo progressInfo) throws IOException;

    private String buildSearchRequestBody(String query, int size) {
        // 如果 query 为空，默认是 match_all
        if (query == null || query.trim().isEmpty()) {
            return "{\"size\":" + size + ",\"query\":{\"match_all\":{}}}";
        }
        return "{\"size\":" + size + ",\"query\":" + query + "}";
    }

    protected List<JSONObject> parseHitsFromResponse(String responseJson) {
        List<JSONObject> result = new ArrayList<>();
        try {
            // 使用fastjson解析响应
            JSONObject jsonObject = JSON.parseObject(responseJson);
            JSONObject hits = jsonObject.getJSONObject("hits");
            if (hits != null) {
                JSONArray hitsArray = hits.getJSONArray("hits");
                if (hitsArray != null) {
                    for (int i = 0; i < hitsArray.size(); i++) {
                        JSONObject hit = hitsArray.getJSONObject(i);
                        if (hit != null) {
                            JSONObject source = hit.getJSONObject("_source");
                            if (source != null) {
                                // 将JSONObject转换为Map
                                result.add(source);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("解析响应数据失败", e);
        }
        return result;
    }

    protected void updateProgress(ProgressInfo progressInfo, int total, int current) {
        if (total <= 0) return;
        int progress = (int) (((double) current / total) * 100);
        progressInfo.setProgress(progress);
        logger.debug("导出进度: {}%", progress);
    }
}