package com.dipper.monitor.service.elastic.dataexport.impl;

import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.dataexport.DataExportService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class DataExportServiceImpl implements DataExportService {

    private static final Logger logger = LoggerFactory.getLogger(DataExportServiceImpl.class);

    @Autowired
    private ElasticClientService elasticClientService;

    @Autowired
    private ElasticRealIndexService elasticRealIndexService;

    // 存储导出任务的进度
    private final Map<String, Map<String, Object>> exportTasks = new ConcurrentHashMap<>();

    // 线程池用于异步处理导出任务
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    public List<String> searchIndices(String keyword) {
        try {
            // 使用 ElasticRealIndexService 获取索引列表
            List<IndexEntity> indexEntities = elasticRealIndexService.listIndexList(true, false, "open");
            List<String> indices = indexEntities.stream()
                    .map(IndexEntity::getIndex)
                    .collect(Collectors.toList());

            if (keyword != null && !keyword.isEmpty()) {
                return indices.stream()
                        .filter(index -> index.contains(keyword))
                        .collect(Collectors.toList());
            }

            return indices;
        } catch (IOException e) {
            logger.error("搜索索引失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public String exportData(String index, String query, String format, int size) {
        // 生成任务ID
        String taskId = UUID.randomUUID().toString();

        // 初始化任务进度
        Map<String, Object> progressInfo = new HashMap<>();
        progressInfo.put("progress", 0);
        progressInfo.put("status", "processing");
        progressInfo.put("format", format);
        progressInfo.put("filePath", "");
        exportTasks.put(taskId, progressInfo);

        // 异步执行导出任务
        executorService.submit(() -> doExport(taskId, index, query, format, size));

        return taskId;
    }

    @Override
    public Map<String, Object> getExportProgress(String taskId) {
        return exportTasks.getOrDefault(taskId, Collections.singletonMap("status", "not_found"));
    }

    private void doExport(String taskId, String index, String query, String format, int size) {
        Map<String, Object> progressInfo = exportTasks.get(taskId);

        try {
            // 构建查询API路径（根据实际接口规则）
            String api = "/api/es/" + index + "/_search";

            // 构造请求体（假设 query 是 JSON 格式的字符串）
            String requestBody = buildSearchRequestBody(query, size);

            // 发送 POST 请求获取数据（注意：executePostApi 返回的是字符串）
            String responseJson = elasticClientService.executePostApi(api, new StringEntity(requestBody));

            // 解析响应中的 hits 数据（此处需根据实际响应结构解析）
            SearchHit[] hits = parseHitsFromResponse(responseJson); // 需要你自己实现

            // 创建导出文件
            String fileName = "export_" + index + "_" + System.currentTimeMillis() + "." + format;
            String filePath = System.getProperty("java.io.tmpdir") + File.separator + fileName;

            // 导出数据
            if ("csv".equals(format)) {
                exportToCsv(hits, filePath);
            } else {
                exportToJson(hits, filePath);
            }

            // 更新进度为完成
            progressInfo.put("progress", 100);
            progressInfo.put("status", "completed");
            progressInfo.put("filePath", filePath);

        } catch (Exception e) {
            logger.error("导出数据失败", e);
            progressInfo.put("status", "failed");
            progressInfo.put("error", e.getMessage());
        }
    }

    private String buildSearchRequestBody(String query, int size) {
        // 如果 query 为空，默认是 match_all
        if (query == null || query.trim().isEmpty()) {
            return "{\"size\":" + size + ",\"query\":{\"match_all\":{}}}";
        }
        return "{\"size\":" + size + ",\"query\":" + query + "}";
    }

    // 模拟 SearchHit 解析函数（需替换为真实解析逻辑）
    private SearchHit[] parseHitsFromResponse(String responseJson) {
        // 实际应使用 JSON 解析库（如 Jackson/Gson）解析 responseJson 中的 hits 字段
        // 这里简化为返回空数组
        return new SearchHit[0];
    }

    private void exportToJson(SearchHit[] hits, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("[\n");

            for (int i = 0; i < hits.length; i++) {
                SearchHit hit = hits[i];
                writer.write(hit.getSourceAsString());

                if (i < hits.length - 1) {
                    writer.write(",\n");
                }

                updateProgress(hits.length, i);
            }

            writer.write("\n]");
        }
    }

    private void exportToCsv(SearchHit[] hits, String filePath) throws IOException {
        if (hits.length == 0) {
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write("");
            }
            return;
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            // 获取第一条数据的所有字段作为CSV的表头
            Map<String, Object> firstHit = hits[0].getSourceAsMap();
            List<String> headers = new ArrayList<>(firstHit.keySet());

            // 写入表头
            writer.write(String.join(",", headers) + "\n");

            // 写入数据
            for (int i = 0; i < hits.length; i++) {
                SearchHit hit = hits[i];
                Map<String, Object> source = hit.getSourceAsMap();

                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    Object value = source.get(header);
                    values.add(value != null ? escapeCSV(value.toString()) : "");
                }

                writer.write(String.join(",", values) + "\n");

                // 更新进度
                updateProgress(hits.length, i);
            }
        }
    }

    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void updateProgress(int total, int current) {
        if (total <= 0) return;
        int progress = (int) (((double) current / total) * 100);
        logger.debug("导出进度: {}%", progress);
    }

    // 模拟 SearchHit 类（仅用于示例）
    private static class SearchHit {
        private String sourceAsString;
        private Map<String, Object> sourceAsMap;

        public SearchHit(String sourceAsString, Map<String, Object> sourceAsMap) {
            this.sourceAsString = sourceAsString;
            this.sourceAsMap = sourceAsMap;
        }

        public String getSourceAsString() {
            return sourceAsString;
        }

        public Map<String, Object> getSourceAsMap() {
            return sourceAsMap;
        }
    }
}