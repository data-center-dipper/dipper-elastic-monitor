package com.dipper.monitor.service.elastic.thread.handlers.realcheck;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadCharReq;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadCheckResult;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadCheckItem;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadPoolSuggestion;
import com.dipper.monitor.entity.elastic.thread.check.yanshi.ThreadPoolItem;
import com.dipper.monitor.service.elastic.thread.ThreadManagerService;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于历史数据进行线程池健康检查的处理器
 */
@Data
public class ThreadHistoryCheckHandler {

    private ThreadManagerService threadManagerService;

    public ThreadHistoryCheckHandler() {
        this.threadManagerService = SpringUtil.getBean(ThreadManagerService.class);
    }

    /**
     * 根据历史数据执行线程池健康检查
     *
     * @param threadPoolItems 线程池节点信息列表
     * @return 检查结果
     */
    public ThreadCheckResult threadHistoryTimeCheck(List<ThreadPoolItem> threadPoolItems) {
        if (CollectionUtils.isEmpty(threadPoolItems)) {
            return buildEmptyResult("未找到有效的线程池节点信息");
        }

        // 获取最近1小时内的线程池指标数据
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);

        // 构建请求参数
        ThreadCharReq threadCharReq = new ThreadCharReq();
        threadCharReq.setClusterCode(ElasticBeanUtils.getCurrentCluster().getClusterCode());

        // 设置时间格式（可根据项目中定义的时间格式调整）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        threadCharReq.setStartTime(oneHourAgo.format(formatter));
        threadCharReq.setEndTime(now.format(formatter));

        // 查询数据库中的历史指标数据
        List<ThreadMetricEntity> metrics = threadManagerService.getThreadMetrics(threadCharReq);

        if (CollectionUtils.isEmpty(metrics)) {
            return buildEmptyResult("未查询到最近的线程池监控数据");
        }

        // 构建检查项
        List<ThreadCheckItem> checkItems = buildCheckItems(metrics, threadPoolItems);

        // 计算状态
        String overallStatus = calculateOverallStatus(checkItems);
        String readStatus = calculateIoStatus(checkItems, Arrays.asList("index", "search"));
        String writeStatus = calculateIoStatus(checkItems, Arrays.asList("bulk", "write"));

        // 构建建议
        List<ThreadPoolSuggestion> suggestions = generateSuggestions(checkItems);

        // 组装最终结果
        return new ThreadCheckResult()
                .setOverallStatus(overallStatus)
                .setReadStatus(readStatus)
                .setWriteStatus(writeStatus)
                .setMessage("部分线程池负载偏高，请关注")
                .setCheckItems(checkItems)
                .setSuggestions(suggestions);
    }

    /**
     * 构建检查项列表
     */
    private List<ThreadCheckItem> buildCheckItems(List<ThreadMetricEntity> metrics,
                                                  List<ThreadPoolItem> threadPoolItems) {
        Map<String, ThreadPoolItem> nodeMap = threadPoolItems.stream()
                .collect(Collectors.toMap(
                        item -> item.getNodeName() + ":" + item.getName(),
                        item -> item));

        return metrics.stream()
                .filter(metric -> nodeMap.containsKey(metric.getNodeName() + ":" + metric.getThreadType()))
                .map(metric -> {
                    ThreadPoolItem item = nodeMap.get(metric.getNodeName() + ":" + metric.getThreadType());
                    String name = item.getNodeName();

                    // 当前队列大小
                    int queueSize = metric.getQueueSize() != null ? metric.getQueueSize() : 0;

                    // 阈值判断：默认 <100
                    String threshold = "<100";
                    String status = queueSize > 100 ? "警告" : "正常";

                    return new ThreadCheckItem()
                            .setName(name)
                            .setCategory(metric.getThreadType())
                            .setItem("等待队列长度")
                            .setValue(String.valueOf(queueSize))
                            .setThreshold(threshold)
                            .setStatus(status)
                            .setDescription("线程池任务较多，需关注");
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建空结果
     */
    private ThreadCheckResult buildEmptyResult(String message) {
        return new ThreadCheckResult()
                .setOverallStatus("正常")
                .setReadStatus("正常")
                .setWriteStatus("正常")
                .setMessage(message)
                .setCheckItems(Collections.emptyList())
                .setSuggestions(Collections.emptyList());
    }

    /**
     * 计算整体状态：只要有一个“警告”或“严重”，就提升级别
     */
    private String calculateOverallStatus(List<ThreadCheckItem> items) {
        if (items.stream().anyMatch(i -> "严重".equals(i.getStatus()))) {
            return "严重";
        }
        if (items.stream().anyMatch(i -> "警告".equals(i.getStatus()))) {
            return "警告";
        }
        return "正常";
    }

    /**
     * 计算 IO 类别状态（如读取/写入）
     */
    private String calculateIoStatus(List<ThreadCheckItem> items, List<String> categories) {
        List<ThreadCheckItem> filtered = items.stream()
                .filter(i -> categories.contains(i.getCategory()))
                .toList();

        if (filtered.isEmpty()) {
            return "正常";
        }

        if (filtered.stream().anyMatch(i -> "严重".equals(i.getStatus()))) {
            return "压力过大";
        }
        if (filtered.stream().anyMatch(i -> "警告".equals(i.getStatus()))) {
            return "压力较大";
        }
        return "正常";
    }

    /**
     * 生成优化建议
     */
    private List<ThreadPoolSuggestion> generateSuggestions(List<ThreadCheckItem> items) {
        Map<String, Integer> warnCategories = new HashMap<>();

        for (ThreadCheckItem item : items) {
            if ("警告".equals(item.getStatus())) {
                warnCategories.put(item.getCategory(), warnCategories.getOrDefault(item.getCategory(), 0) + 1);
            }
        }

        List<ThreadPoolSuggestion> suggestions = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : warnCategories.entrySet()) {
            String category = entry.getKey();
            ThreadPoolSuggestion suggestion = new ThreadPoolSuggestion()
                    .setTitle("优化建议：调整 " + category + " 线程池参数")
                    .setContent(category + " 类型线程池存在负载过高风险，建议增加核心线程数或调整拒绝策略")
                    .setActions(Arrays.asList(
                            "扩大线程池",
                            "调整队列大小",
                            "监控拒绝率"
                    ));
            suggestions.add(suggestion);
        }

        return suggestions;
    }
}