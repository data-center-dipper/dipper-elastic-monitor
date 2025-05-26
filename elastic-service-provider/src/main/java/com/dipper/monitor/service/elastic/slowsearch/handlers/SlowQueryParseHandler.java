package com.dipper.monitor.service.elastic.slowsearch.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Elasticsearch慢查询解析处理器
 */
public class SlowQueryParseHandler {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 解析Elasticsearch慢查询API响应
     * @param response Elasticsearch API响应内容
     * @param clusterCode 集群编码
     * @param slowQueryThreshold 慢查询阈值(毫秒)
     * @return 慢查询实体列表
     */
    public List<SlowQueryEntity> parseSlowQueryResponse(String response, String clusterCode, int slowQueryThreshold) {
        List<SlowQueryEntity> result = new ArrayList<>();

        if (StringUtils.isBlank(response)) {
            return result;
        }

        try {
            JSONObject responseJson = JSON.parseObject(response);
            JSONObject nodesJson = responseJson.getJSONObject("nodes");

            if (nodesJson == null || nodesJson.isEmpty()) {
                return result;
            }

            // 遍历所有节点
            for (String nodeId : nodesJson.keySet()) {
                JSONObject nodeJson = nodesJson.getJSONObject(nodeId);
                String nodeName = nodeJson.getString("name");

                // 获取索引统计信息
                JSONObject indicesJson = nodeJson.getJSONObject("indices");
                if (indicesJson == null) {
                    continue;
                }

                // 获取搜索统计信息
                JSONObject searchJson = indicesJson.getJSONObject("search");
                if (searchJson == null) {
                    continue;
                }

                // 获取查询统计信息
                JSONObject queriesJson = searchJson.getJSONObject("query");
                if (queriesJson == null) {
                    continue;
                }

                // 获取慢查询列表
                JSONArray slowLogsArray = queriesJson.getJSONArray("slow_log");
                if (slowLogsArray == null || slowLogsArray.isEmpty()) {
                    continue;
                }

                // 处理每个慢查询记录
                for (int i = 0; i < slowLogsArray.size(); i++) {
                    JSONObject slowLog = slowLogsArray.getJSONObject(i);

                    // 提取执行时间
                    long executionTimeMs = slowLog.getLongValue("took_millis");

                    // 只处理超过阈值的查询
                    if (executionTimeMs < slowQueryThreshold) {
                        continue;
                    }

                    SlowQueryEntity entity = new SlowQueryEntity();
                    entity.setClusterCode(clusterCode);
                    entity.setNodeId(nodeId);
                    entity.setNodeName(nodeName);

                    // 设置任务ID，如果没有则生成一个UUID
                    String taskId = slowLog.getString("task_id");
                    if (StringUtils.isBlank(taskId)) {
                        taskId = UUID.randomUUID().toString();
                    }
                    entity.setTaskId(taskId);

                    // 设置索引名称
                    String indexName = slowLog.getString("index");
                    entity.setIndexName(indexName);

                    // 设置查询类型
                    String queryType = determineQueryType(slowLog.getString("source"));
                    entity.setQueryType(queryType);

                    // 设置执行时间（毫秒）
                    entity.setExecutionTimeMs(executionTimeMs);

                    // 设置开始时间
                    String startTimeStr = slowLog.getString("start_time");
                    Date startTime = StringUtils.isNotBlank(startTimeStr) ? parseDate(startTimeStr) : new Date();
                    entity.setStartTime(startTime);

                    // 设置状态，默认为已完成
                    entity.setStatus("completed");

                    // 设置查询内容
                    String queryContent = slowLog.getString("source");
                    entity.setQueryContent(queryContent);

                    // 设置描述（可选）
                    String description = "Slow log entry from [" + indexName + "]";
                    if (StringUtils.isNotBlank(queryContent) && queryContent.length() > 100) {
                        description = queryContent.substring(0, 100);
                    } else {
                        description = queryContent;
                    }
                    entity.setDescription(description);

                    // 设置堆栈信息，如果有的话
                    String stackTrace = slowLog.getString("stack_trace");
                    entity.setStackTrace(stackTrace);

                    // 设置采集时间
                    entity.setCollectTime(new Date());

                    // 设置是否已处理（默认未处理）
                    entity.setIsProcessed(0);

                    result.add(entity);
                }
            }

            return result;
        } catch (Exception e) {
            // 发生异常时返回空列表
            return new ArrayList<>();
        }
    }

    /**
     * 根据查询内容确定查询类型
     * @param queryContent 查询内容
     * @return 查询类型：search, aggregation, scroll
     */
    private String determineQueryType(String queryContent) {
        if (StringUtils.isBlank(queryContent)) {
            return "search";
        }

        queryContent = queryContent.toLowerCase();

        if (queryContent.contains("aggs") || queryContent.contains("aggregations")) {
            return "aggregation";
        } else if (queryContent.contains("scroll")) {
            return "scroll";
        } else {
            return "search";
        }
    }

    /**
     * 解析日期字符串
     * @param dateStr 日期字符串（格式：yyyy-MM-dd HH:mm:ss）
     * @return 解析后的日期对象
     */
    private Date parseDate(String dateStr) {
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (Exception e) {
            return new Date();
        }
    }
}