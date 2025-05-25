package com.dipper.monitor.service.elastic.thread.handlers.realcheck;


import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadPoolStat;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadCheckItem;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadCheckResult;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadPoolSuggestion;
import com.dipper.monitor.entity.elastic.thread.check.yanshi.ThreadPoolItem;
import com.dipper.monitor.entity.elastic.thread.hot.ThreadHotView;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.thread.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


import java.util.*;

public class ThreadRealTimeCheckHandler {

    private static final Logger log = LoggerFactory.getLogger(ThreadRealTimeCheckHandler.class);
    private ElasticClientService elasticClientService;
    private ThreadPoolService threadPoolService;

    public ThreadRealTimeCheckHandler(ElasticClientService elasticClientService,ThreadPoolService threadPoolService) {
        this.elasticClientService = elasticClientService;
        this.threadPoolService = threadPoolService;
    }

    /**
     * 实时检查线程状态并生成检测报告
     */
    public ThreadCheckResult threadRealTimeCheck(List<ThreadHotView> cachedThreadList, List<ThreadPoolItem> threadPoolItems) {
        ThreadCheckResult result = new ThreadCheckResult();
        List<ThreadCheckItem> checkItems = new ArrayList<>();
        List<ThreadPoolSuggestion> suggestions = new ArrayList<>();

        try {
            // 统计各类型线程数量
            Map<String, Integer> typeCountMap = new HashMap<>();
            int highCpuCount = 0;
            int blockedCount = 0;

            for (ThreadHotView thread : cachedThreadList) {
                String type = thread.getType();
                typeCountMap.put(type, typeCountMap.getOrDefault(type, 0) + 1);

                if (thread.getCpu() != null && thread.getCpu() > 80) {
                    highCpuCount++;
                }

                if (thread.getStackTrace() != null &&
                        Arrays.asList("BLOCKED", "WAITING", "TIMED_WAITING")
                                .stream().anyMatch(thread.getStackTrace()::contains)) {
                    blockedCount++;
                }
            }

            // 添加各类线程检查项
            addThreadTypeCheck(checkItems, result, typeCountMap, "搜索线程", 50, 100, "读写性能");
            addThreadTypeCheck(checkItems, result, typeCountMap, "写入线程", 30, 60, "写入性能");
            addThreadTypeCheck(checkItems, result, typeCountMap, "批量线程", 20, 40, "批量操作");
            addThreadTypeCheck(checkItems, result, typeCountMap, "管理线程", 10, 20, "系统资源");

            // 添加高CPU线程检查项
            addThresholdCheck(checkItems, result, "系统资源", "高CPU使用率线程数", highCpuCount, 5, 10, "系统负载过高");

            // 添加阻塞线程检查项
            addThresholdCheck(checkItems, result, "线程阻塞", "阻塞线程数", blockedCount, 3, 5, "可能死锁或资源竞争");

            // 线程池检查
            checkThreadPoolStats(result, checkItems, suggestions);

            // 生成建议
            generateSuggestions(suggestions, typeCountMap.getOrDefault("批量线程", 0),
                    typeCountMap.getOrDefault("搜索线程", 0), blockedCount);

            // 最终设置结果
            result.setCheckItems(checkItems);
            result.setSuggestions(suggestions);
            updateFinalStatus(result);

        } catch (Exception e) {
            log.error("线程实时检测异常：{}", e.getMessage(), e);
            result.setOverallStatus("异常");
            result.setMessage("线程检测过程中发生错误：" + e.getMessage());
        }

        return result;
    }

    // 通用线程类型检查方法
    private void addThreadTypeCheck(List<ThreadCheckItem> checkItems,
                                    ThreadCheckResult result,
                                    Map<String, Integer> typeCountMap,
                                    String type,
                                    int normalThreshold,
                                    int warningThreshold,
                                    String categoryPrefix) {
        int count = typeCountMap.getOrDefault(type, 0);
        String item = type + "数";
        String descriptionNormal = type + "数量正常";
        String descriptionWarning = type + "数量较多，需要关注";
        String descriptionCritical = type + "数量过多，可能导致资源竞争";

        ThreadCheckItem itemObj = createThresholdCheck(
                categoryPrefix, item, String.valueOf(count),
                "< " + normalThreshold,
                count, normalThreshold, warningThreshold,
                descriptionNormal, descriptionWarning, descriptionCritical);

        checkItems.add(itemObj);

        if ("搜索线程".equals(type)) {
            updateReadStatus(result, itemObj.getStatus());
        } else if ("写入线程".equals(type) || "批量线程".equals(type)) {
            updateWriteStatus(result, itemObj.getStatus());
        }

        updateOverallStatus(result, itemObj.getStatus());
    }

    // 通用数值型阈值检查方法
    private ThreadCheckItem createThresholdCheck(String category, String item,
                                                 String value, String threshold,
                                                 int actualValue, int normalThreshold,
                                                 int warningThreshold,
                                                 String descNormal, String descWarning, String descCritical) {
        ThreadCheckItem checkItem = new ThreadCheckItem();
        checkItem.setCategory(category);
        checkItem.setItem(item);
        checkItem.setValue(value);
        checkItem.setThreshold(threshold);

        if (actualValue > warningThreshold) {
            checkItem.setStatus("严重");
            checkItem.setDescription(descCritical);
        } else if (actualValue > normalThreshold) {
            checkItem.setStatus("警告");
            checkItem.setDescription(descWarning);
        } else {
            checkItem.setStatus("正常");
            checkItem.setDescription(descNormal);
        }

        return checkItem;
    }

    // 通用阈值检查方法
    private void addThresholdCheck(List<ThreadCheckItem> checkItems,
                                   ThreadCheckResult result,
                                   String category, String item,
                                   int count, int normalThreshold, int warningThreshold,
                                   String descriptionSuffix) {
        String value = String.valueOf(count);
        String threshold = "< " + normalThreshold;
        String descNormal = "数量正常";
        String descWarning = "数量较多，" + descriptionSuffix;
        String descCritical = "数量过多，" + descriptionSuffix;

        ThreadCheckItem checkItem = createThresholdCheck(
                category, item, value, threshold,
                count, normalThreshold, warningThreshold,
                descNormal, descWarning, descCritical);

        checkItems.add(checkItem);
        updateOverallStatus(result, checkItem.getStatus());
    }

    // 更新整体状态
    private void updateOverallStatus(ThreadCheckResult result, String status) {
        if ("严重".equals(status)) {
            result.setOverallStatus("异常");
        } else if ("警告".equals(status) && !"异常".equals(result.getOverallStatus())) {
            result.setOverallStatus("警告");
        }
    }

    // 更新读取状态
    private void updateReadStatus(ThreadCheckResult result, String status) {
        if ("严重".equals(status)) {
            result.setReadStatus("压力过大");
        } else if ("警告".equals(status) && !"压力过大".equals(result.getReadStatus())) {
            result.setReadStatus("压力较大");
        }
    }

    // 更新写入状态
    private void updateWriteStatus(ThreadCheckResult result, String status) {
        if ("严重".equals(status)) {
            result.setWriteStatus("压力过大");
        } else if ("警告".equals(status) && !"压力过大".equals(result.getWriteStatus())) {
            result.setWriteStatus("压力较大");
        }
    }

    // 设置最终状态
    private void updateFinalStatus(ThreadCheckResult result) {
        if (result.getCheckItems().isEmpty()) {
            result.setOverallStatus("未检测");
        }
    }

    // 生成优化建议
    private void generateSuggestions(List<ThreadPoolSuggestion> suggestions,
                                     int batchCount, int searchCount, int blockedCount) {
        if (batchCount > 20) {
            ThreadPoolSuggestion suggestion = new ThreadPoolSuggestion();
            suggestion.setTitle("批量线程压力过大");
            suggestion.setContent("当前批量线程数量较多，可能导致系统资源竞争，影响整体性能。");
            suggestion.setActions(Arrays.asList(
                    "调整批量操作的大小和频率",
                    "增加批量线程池的大小",
                    "检查是否有不必要的批量操作"
            ));
            suggestions.add(suggestion);
        }

        if (searchCount > 50) {
            ThreadPoolSuggestion suggestion = new ThreadPoolSuggestion();
            suggestion.setTitle("搜索性能下降");
            suggestion.setContent("当前搜索线程数量较多，可能导致搜索性能下降。");
            suggestion.setActions(Arrays.asList(
                    "优化复杂查询，减少字段和过滤条件",
                    "增加缓存层，减少ES查询次数",
                    "考虑增加节点或分片分散压力"
            ));
            suggestions.add(suggestion);
        }

        if (blockedCount > 3) {
            ThreadPoolSuggestion suggestion = new ThreadPoolSuggestion();
            suggestion.setTitle("线程阻塞问题");
            suggestion.setContent("当前存在较多阻塞线程，可能存在死锁或资源竞争问题。");
            suggestion.setActions(Arrays.asList(
                    "分析堆栈信息查找阻塞原因",
                    "检查是否存在死锁情况",
                    "优化锁使用，减少持有时间"
            ));
            suggestions.add(suggestion);
        }
    }

    // 线程池统计检查
    private void checkThreadPoolStats(ThreadCheckResult result,
                                      List<ThreadCheckItem> checkItems,
                                      List<ThreadPoolSuggestion> suggestions) {
        List<ThreadPoolStat> poolStats = threadPoolService.fetchThreadPoolStats();

        for (ThreadPoolStat stat : poolStats) {
            // 队列长度检查
            ThreadCheckItem queueItem = createThresholdCheck(
                    "线程池[" + stat.getName() + "]",
                    "等待队列长度",
                    String.valueOf(stat.getQueue()),
                    "< 100",
                    stat.getQueue(), 100, 200,
                    "队列长度正常",
                    "队列较多，需关注任务执行",
                    "队列积压严重，可能影响响应"
            );
            checkItems.add(queueItem);
            updateOverallStatus(result, queueItem.getStatus());

            // 活跃线程检查
            double activeRatio = ((double) stat.getActive() / stat.getSize()) * 100;
            ThreadCheckItem activeItem = createThresholdCheck(
                    "线程池[" + stat.getName() + "]",
                    "活跃线程数",
                    String.valueOf(stat.getActive()),
                    "< 80%",
                    (int) activeRatio, 80, 95,
                    "负载正常",
                    "负载较高，需关注",
                    "满负荷运行，可能导致延迟"
            );
            checkItems.add(activeItem);
            updateOverallStatus(result, activeItem.getStatus());

            // 建议生成
            if (stat.getQueue() > 100) {
                ThreadPoolSuggestion suggestion = new ThreadPoolSuggestion();
                suggestion.setTitle("线程池[" + stat.getName() + "] 队列积压");
                suggestion.setContent("当前线程池队列长度较大，可能存在性能瓶颈。");
                suggestion.setActions(Arrays.asList(
                        "增加线程池核心线程数",
                        "优化任务处理逻辑",
                        "将部分任务异步化或限流"
                ));
                suggestions.add(suggestion);
            }

            if (activeRatio > 80) {
                ThreadPoolSuggestion suggestion = new ThreadPoolSuggestion();
                suggestion.setTitle("线程池[" + stat.getName() + "] 负载过高");
                suggestion.setContent("线程池活跃线程比例较高，系统资源可能紧张。");
                suggestion.setActions(Arrays.asList(
                        "调整线程池配置",
                        "分析任务来源，优化高频任务",
                        "部署更多节点来分担负载"
                ));
                suggestions.add(suggestion);
            }
        }
    }
}