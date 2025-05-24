package com.dipper.monitor.service.elastic.thread.handlers;

import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadChartSummary;

import java.util.*;
import java.util.stream.Collectors;

public class ThreadChartSummaryHandler {

    private final List<ThreadMetricEntity> threadMetrics;

    public ThreadChartSummaryHandler(List<ThreadMetricEntity> threadMetrics) {
        this.threadMetrics = threadMetrics;
    }

    public List<ThreadChartSummary> threadChartSummary() {
        if (threadMetrics == null || threadMetrics.isEmpty()) {
            return Collections.emptyList();
        }

        // 按 threadType 分组
        Map<String, List<ThreadMetricEntity>> groupedByType = threadMetrics.stream()
                .collect(Collectors.groupingBy(ThreadMetricEntity::getThreadType));

        List<ThreadChartSummary> result = new ArrayList<>();

        for (Map.Entry<String, List<ThreadMetricEntity>> entry : groupedByType.entrySet()) {
            String threadType = entry.getKey();
            List<ThreadMetricEntity> metrics = entry.getValue();

            // 提取 activeThreads 值列表
            List<Integer> activeThreadsList = metrics.stream()
                    .map(ThreadMetricEntity::getActiveThreads)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (activeThreadsList.isEmpty()) {
                continue; // 跳过无有效数据的线程类型
            }

            double sum = activeThreadsList.stream().mapToDouble(Integer::doubleValue).sum();
            double max = activeThreadsList.stream().mapToDouble(Integer::doubleValue).max().orElse(0);
            double min = activeThreadsList.stream().mapToDouble(Integer::doubleValue).min().orElse(0);
            double avg = sum / activeThreadsList.size();

            result.add(new ThreadChartSummary(
                    threadType,
                    max,
                    min,
                    avg,
                    sum
            ));
        }

        return result;
    }
}