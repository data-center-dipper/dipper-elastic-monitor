package com.dipper.monitor.service.elastic.thread.handlers;

import com.dipper.monitor.entity.elastic.thread.check.pool.GroupKey;
import com.dipper.monitor.entity.elastic.thread.check.pool.ThreadPoolTrendResult;
import com.dipper.monitor.entity.elastic.thread.check.yanshi.ThreadPoolItem;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.thread.ThreadPoolService;
import com.dipper.monitor.service.elastic.thread.handlers.pool.ThreadPoolAnalyzer;

import java.io.IOException;
import java.util.*;

public class ThreadPoolCheckHandler {

    private ElasticClientService elasticClientService;
    private ThreadPoolService threadPoolService;

    public ThreadPoolCheckHandler(ElasticClientService elasticClientService, ThreadPoolService threadPoolService) {
        this.elasticClientService = elasticClientService;
        this.threadPoolService = threadPoolService;
    }

    public  List<ThreadPoolTrendResult> threadPoolCheck() throws IOException {

        // Step 1: 采集5次数据
        List<List<ThreadPoolItem>> allData = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<ThreadPoolItem> stats = threadPoolService.fetchThreadPool();
            allData.add(stats);
            try {
                Thread.sleep(1000); // 每次间隔1秒
            } catch (InterruptedException ignored) {}
        }

        // Step 2: 按节点和线程池名称分组
        Map<GroupKey, List<ThreadPoolItem>> groupedMap = new HashMap<>();
        for (List<ThreadPoolItem> dataList : allData) {
            for (ThreadPoolItem item : dataList) {
                String key = item.getNodeName() + "-" + item.getName();
                GroupKey groupKey = new GroupKey(key, item.getNodeName(),item.getName());
                groupedMap.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(item);
            }
        }

        // Step 3: 分析每组是否有趋势变化
        List<ThreadPoolTrendResult> trendResults = new ArrayList<>();

        for (Map.Entry<GroupKey, List<ThreadPoolItem>> entry : groupedMap.entrySet()) {
            GroupKey groupKey = entry.getKey();
            String nodeName = groupKey.getNodeName();
            String poolName = groupKey.getName();
            List<ThreadPoolItem> items = entry.getValue();

            ThreadPoolTrendResult trendResult = ThreadPoolAnalyzer.analyzeTrend(nodeName, poolName, items);
            if (trendResult != null) {
                trendResults.add(trendResult);
            }
        }

        Collections.sort(trendResults, Comparator.comparing(ThreadPoolTrendResult::getNodeName));

        return trendResults;
    }

    /**
     * 根据趋势结果评估整体状态
     */
    private String evaluateStatus(List<ThreadPoolTrendResult> trendResults) {
        boolean hasSevere = trendResults.stream()
                .anyMatch(r -> r.getLastQueue() > 200 || r.getLastActive() > getDefaultThreadPoolSize(r.getPoolName()) * 0.95);

        boolean hasWarning = trendResults.stream()
                .anyMatch(r -> r.getLastQueue() > 100 || r.getLastActive() > getDefaultThreadPoolSize(r.getPoolName()) * 0.8);

        if (hasSevere) return "异常";
        else if (hasWarning) return "警告";
        else return "正常";
    }

    private int getDefaultThreadPoolSize(String poolName) {
        switch (poolName) {
            case "bulk": return 8;
            case "index": return 8;
            case "search": return 12;
            default: return 4;
        }
    }
}