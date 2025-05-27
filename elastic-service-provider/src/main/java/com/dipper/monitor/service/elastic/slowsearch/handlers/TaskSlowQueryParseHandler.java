package com.dipper.monitor.service.elastic.slowsearch.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.slowsearch.task.SlowQueryTaskEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class TaskSlowQueryParseHandler {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 解析 Elasticsearch 的 _tasks?actions=*search&detailed 接口响应
     *
     * @param response          响应内容（JSON 字符串）
     * @param clusterCode       集群编码
     * @param slowQueryThreshold 慢查询阈值（毫秒）
     * @return 慢查询任务列表
     */
    public List<SlowQueryTaskEntity> parseSlowQueryResponse(String response, String clusterCode, int slowQueryThreshold) {
        List<SlowQueryTaskEntity> result = new ArrayList<>();

        if (StringUtils.isBlank(response)) {
            return result;
        }

        try {
            JSONObject json = JSON.parseObject(response);
            JSONArray tasks = json.getJSONArray("tasks");

            if (tasks == null || tasks.isEmpty()) {
                return result;
            }

            for (int i = 0; i < tasks.size(); i++) {
                JSONObject task = tasks.getJSONObject(i);

                String nodeId = task.getString("node");
                String taskId = task.getString("id");
                String action = task.getString("action");
                String description = task.getString("description");
                long startTime = task.getLongValue("start_time");
                long runningTimeNanos = task.getLongValue("running_time_in_nanos");
                long executionTimeMillis = runningTimeNanos / 1_000_000; // 纳秒转毫秒

                // 判断是否是慢查询
                if (executionTimeMillis < slowQueryThreshold) {
                    continue;
                }

                // 构造实体对象
                SlowQueryTaskEntity entity = new SlowQueryTaskEntity();
                entity.setClusterCode(clusterCode);
                entity.setNodeId(nodeId);
                entity.setNodeName(""); // 如果有节点名可以从其他接口获取或传入
                entity.setTaskId(taskId);
                entity.setAction(action);
                entity.setDescription(description);
                entity.setExecutionTime(executionTimeMillis);
                entity.setStartTime(DATE_FORMAT.format(new Date(startTime)));
                entity.setQueryType(determineQueryType(description));
                entity.setStatus("running"); // 当前任务肯定是运行中的
                entity.setCollectTime(new Date());

                result.add(entity);
            }

        } catch (Exception e) {
          log.error("解析慢查询任务失败", e);
        }

        return result;
    }

    /**
     * 根据查询描述判断查询类型
     */
    private String determineQueryType(String description) {
        if (StringUtils.isBlank(description)) {
            return "unknown";
        }

        description = description.toLowerCase();

        if (description.contains("aggs") || description.contains("aggregations")) {
            return "aggregation";
        } else if (description.contains("scroll")) {
            return "scroll";
        } else if (description.contains("suggest")) {
            return "suggest";
        } else {
            return "search";
        }
    }
}