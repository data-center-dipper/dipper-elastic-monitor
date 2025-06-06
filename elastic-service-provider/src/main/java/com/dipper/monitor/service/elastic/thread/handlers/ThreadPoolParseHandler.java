package com.dipper.monitor.service.elastic.thread.handlers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.utils.mock.MockAllData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ThreadPoolParseHandler {

    // 线程类型枚举（可根据实际情况扩展）
    private static final String[] THREAD_TYPES = {
            "bulk", "index", "search", "write", "management", "refresh", "merge"
    };

    public List<ThreadMetricEntity> parseThreadPoolResponse(String response, String clusterCode) {
//        if(ApplicationUtils.isWindows()){
//            return MockAllData.parseThreadPoolResponse(clusterCode);
//        }
        JSONObject rootNode = JSONObject.parseObject(response);
        JSONObject nodes = rootNode.getJSONObject("nodes");

        List<ThreadMetricEntity> result = new ArrayList<>();

        for (String nodeId : nodes.keySet()) {
            JSONObject node = nodes.getJSONObject(nodeId);
            if (node == null || !node.containsKey("thread_pool")) continue;

            JSONObject threadPoolNode = node.getJSONObject("thread_pool");
            String nodeName = node.getString("name"); // 获取节点名称

            for (String threadType : THREAD_TYPES) {
                if (!threadPoolNode.containsKey(threadType)) continue;

                JSONObject poolNode = threadPoolNode.getJSONObject(threadType);

                ThreadMetricEntity metric = new ThreadMetricEntity();
                metric.setClusterCode(clusterCode);
                metric.setNodeName(nodeName);
                metric.setThreadType(threadType);
                metric.setActiveThreads(poolNode.getIntValue("active"));
                metric.setQueueSize(poolNode.getIntValue("queue"));
                metric.setRejectedCount(poolNode.getLongValue("rejected"));
                metric.setCompletedCount(poolNode.getLongValue("completed"));
                metric.setLargestSize(poolNode.getIntValue("largest"));

                metric.setCollectTime(LocalDateTime.now());

                result.add(metric);
            }
        }

        return result;
    }
}