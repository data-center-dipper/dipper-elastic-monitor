//package com.dipper.monitor.service.elastic.thread.handlers;
//
//import com.dipper.monitor.entity.elastic.thread.check.ThreadCheckItem;
//import com.dipper.monitor.entity.elastic.thread.check.ThreadCheckResult;
//import com.dipper.monitor.entity.elastic.thread.hot.ThreadHotView;
//import com.dipper.monitor.entity.elastic.thread.check.ThreadPoolSuggestion;
//import com.dipper.monitor.service.elastic.client.ElasticClientService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ThreadRealTimeCheckHandler {
//
//    private static final Logger log = LoggerFactory.getLogger(ThreadRealTimeCheckHandler.class);
//
//    private ElasticClientService elasticClientService;
//
//    public ThreadRealTimeCheckHandler(ElasticClientService elasticClientService) {
//        this.elasticClientService = elasticClientService;
//    }
//
//    /**
//     * 实时检查线程状态并生成检测报告（基于已获取的热点线程列表）
//     */
//    public ThreadPoolCheckResult threadRealTimeCheck(List<ThreadHotView> cachedThreadList) {
//        ThreadPoolCheckResult result = new ThreadCheckResult();
//        List<ThreadCheckItem> checkItems = new ArrayList<>();
//        List<ThreadPoolSuggestion> suggestions = new ArrayList<>();
//
//        // 默认状态为正常
//        result.setOverallStatus("正常");
//        result.setReadStatus("正常");
//        result.setWriteStatus("正常");
//
//        try {
//            // 统计各类线程数量和状态
//            int searchThreadCount = 0;
//            int writeThreadCount = 0;
//            int batchThreadCount = 0;
//            int managementThreadCount = 0;
//            int highCpuThreadCount = 0;
//            int blockedThreadCount = 0;
//
//            for (ThreadHotView thread : cachedThreadList) {
//                // 统计各类型线程
//                switch (thread.getType()) {
//                    case "搜索线程":
//                        searchThreadCount++;
//                        break;
//                    case "写入线程":
//                        writeThreadCount++;
//                        break;
//                    case "批量线程":
//                        batchThreadCount++;
//                        break;
//                    case "管理线程":
//                        managementThreadCount++;
//                        break;
//                    default:
//                        break;
//                }
//
//                // 统计高CPU使用率线程
//                if (thread.getCpu() != null && thread.getCpu() > 80) {
//                    highCpuThreadCount++;
//                }
//
//                // 统计阻塞线程
//                if (thread.getStackTrace() != null &&
//                        (thread.getStackTrace().contains("BLOCKED") ||
//                                thread.getStackTrace().contains("WAITING") ||
//                                thread.getStackTrace().contains("TIMED_WAITING"))) {
//                    blockedThreadCount++;
//                }
//            }
//
//            // 添加检测项 - 搜索线程
//            addSearchThreadCheck(checkItems, result, searchThreadCount);
//
//            // 添加检测项 - 写入线程
//            addWriteThreadCheck(checkItems, result, writeThreadCount);
//
//            // 添加检测项 - 批量线程
//            addBatchThreadCheck(checkItems, result, batchThreadCount);
//
//            // 添加检测项 - 管理线程
//            addManagementThreadCheck(checkItems, result, managementThreadCount);
//
//            // 添加检测项 - 高CPU线程
//            addHighCpuThreadCheck(checkItems, result, highCpuThreadCount);
//
//            // 添加检测项 - 阻塞线程
//            addBlockedThreadCheck(checkItems, result, blockedThreadCount);
//
//            // 生成优化建议
//            generateSuggestions(suggestions, batchThreadCount, searchThreadCount, blockedThreadCount);
//
//            // 新增：检查线程池统计信息（来自 /cat/thread_pool）
//            checkThreadPoolStats(result, checkItems, suggestions);
//
//            result.setCheckItems(checkItems);
//            result.setSuggestions(suggestions);
//
//        } catch (Exception e) {
//            log.error("线程实时检测异常", e);
//            result.setOverallStatus("异常");
//            result.setMessage("线程检测过程中发生错误：" + e.getMessage());
//        }
//
//        return result;
//    }
//
//    // ========== 各检测项拆分方法（略）==========
//    private void addSearchThreadCheck(List<ThreadCheckItem> checkItems, ThreadCheckResult result, int count) {
//        ThreadCheckItem item = new ThreadCheckItem();
//        item.setCategory("线程数量");
//        item.setItem("搜索线程数");
//        item.setValue(String.valueOf(count));
//        item.setThreshold("< 50");
//
//        if (count > 100) {
//            item.setStatus("严重");
//            item.setDescription("搜索线程数量过多，可能导致资源竞争");
//            result.setReadStatus("压力过大");
//            result.setOverallStatus("异常");
//        } else if (count > 50) {
//            item.setStatus("警告");
//            item.setDescription("搜索线程数量较多，需要关注");
//            result.setReadStatus("压力较大");
//            if (!"异常".equals(result.getOverallStatus())) {
//                result.setOverallStatus("警告");
//            }
//        } else {
//            item.setStatus("正常");
//            item.setDescription("搜索线程数量正常");
//        }
//        checkItems.add(item);
//    }
//
//    private void addWriteThreadCheck(List<ThreadCheckItem> checkItems, ThreadCheckResult result, int count) {
//        ThreadCheckItem item = new ThreadCheckItem();
//        item.setCategory("线程数量");
//        item.setItem("写入线程数");
//        item.setValue(String.valueOf(count));
//        item.setThreshold("< 30");
//
//        if (count > 60) {
//            item.setStatus("严重");
//            item.setDescription("写入线程数量过多，可能导致资源竞争");
//            result.setWriteStatus("压力过大");
//            result.setOverallStatus("异常");
//        } else if (count > 30) {
//            item.setStatus("警告");
//            item.setDescription("写入线程数量较多，需要关注");
//            result.setWriteStatus("压力较大");
//            if (!"异常".equals(result.getOverallStatus())) {
//                result.setOverallStatus("警告");
//            }
//        } else {
//            item.setStatus("正常");
//            item.setDescription("写入线程数量正常");
//        }
//        checkItems.add(item);
//    }
//
//    private void addBatchThreadCheck(List<ThreadCheckItem> checkItems, ThreadCheckResult result, int count) {
//        ThreadCheckItem item = new ThreadCheckItem();
//        item.setCategory("线程数量");
//        item.setItem("批量线程数");
//        item.setValue(String.valueOf(count));
//        item.setThreshold("< 20");
//
//        if (count > 40) {
//            item.setStatus("严重");
//            item.setDescription("批量线程数量过多，可能导致资源竞争");
//            result.setOverallStatus("异常");
//        } else if (count > 20) {
//            item.setStatus("警告");
//            item.setDescription("批量线程数量较多，需要关注");
//            if (!"异常".equals(result.getOverallStatus())) {
//                result.setOverallStatus("警告");
//            }
//        } else {
//            item.setStatus("正常");
//            item.setDescription("批量线程数量正常");
//        }
//        checkItems.add(item);
//    }
//
//    private void addManagementThreadCheck(List<ThreadCheckItem> checkItems, ThreadCheckResult result, int count) {
//        ThreadCheckItem item = new ThreadCheckItem();
//        item.setCategory("线程数量");
//        item.setItem("管理线程数");
//        item.setValue(String.valueOf(count));
//        item.setThreshold("< 10");
//
//        if (count > 20) {
//            item.setStatus("警告");
//            item.setDescription("管理线程数量较多，需要关注");
//            if (!"异常".equals(result.getOverallStatus())) {
//                result.setOverallStatus("警告");
//            }
//        } else {
//            item.setStatus("正常");
//            item.setDescription("管理线程数量正常");
//        }
//        checkItems.add(item);
//    }
//
//    private void addHighCpuThreadCheck(List<ThreadCheckItem> checkItems, ThreadCheckResult result, int count) {
//        ThreadCheckItem item = new ThreadCheckItem();
//        item.setCategory("系统资源");
//        item.setItem("高CPU使用率线程数");
//        item.setValue(String.valueOf(count));
//        item.setThreshold("< 5");
//
//        if (count > 10) {
//            item.setStatus("严重");
//            item.setDescription("高CPU使用率线程数量过多，系统负载过高");
//            result.setOverallStatus("异常");
//        } else if (count > 5) {
//            item.setStatus("警告");
//            item.setDescription("高CPU使用率线程数量较多，系统负载较高");
//            if (!"异常".equals(result.getOverallStatus())) {
//                result.setOverallStatus("警告");
//            }
//        } else {
//            item.setStatus("正常");
//            item.setDescription("高CPU使用率线程数量正常");
//        }
//        checkItems.add(item);
//    }
//
//    private void addBlockedThreadCheck(List<ThreadCheckItem> checkItems, ThreadCheckResult result, int count) {
//        ThreadCheckItem item = new ThreadCheckItem();
//        item.setCategory("线程阻塞");
//        item.setItem("阻塞线程数");
//        item.setValue(String.valueOf(count));
//        item.setThreshold("< 3");
//
//        if (count > 5) {
//            item.setStatus("严重");
//            item.setDescription("阻塞线程数量过多，可能存在死锁或资源竞争");
//            result.setOverallStatus("异常");
//        } else if (count > 3) {
//            item.setStatus("警告");
//            item.setDescription("阻塞线程数量较多，需要关注");
//            if (!"异常".equals(result.getOverallStatus())) {
//                result.setOverallStatus("警告");
//            }
//        } else {
//            item.setStatus("正常");
//            item.setDescription("阻塞线程数量正常");
//        }
//        checkItems.add(item);
//    }
//
//    /**
//     * 根据检测结果生成优化建议
//     */
//    private void generateSuggestions(List<ThreadPoolSuggestion> suggestions,
//                                     int batchThreadCount,
//                                     int searchThreadCount,
//                                     int blockedThreadCount) {
//
//        if (batchThreadCount > 20) {
//            ThreadPoolSuggestion suggestion = new ThreadPoolSuggestion();
//            suggestion.setTitle("批量线程压力过大");
//            suggestion.setContent("当前批量线程数量较多，可能导致系统资源竞争，影响整体性能。");
//            List<String> actions = new ArrayList<>();
//            actions.add("调整批量操作的大小和频率，避免同时执行过多批量操作");
//            actions.add("增加批量线程池的大小，但需要确保系统有足够的资源");
//            actions.add("检查是否有不必要的批量操作，可以合并或取消");
//            suggestion.setActions(actions);
//            suggestions.add(suggestion);
//        }
//
//        if (searchThreadCount > 50) {
//            ThreadPoolSuggestion suggestion = new ThreadPoolSuggestion();
//            suggestion.setTitle("搜索性能下降");
//            suggestion.setContent("当前搜索线程数量较多，可能导致搜索性能下降。");
//            List<String> actions = new ArrayList<>();
//            actions.add("优化复杂查询，减少不必要的字段和过滤条件");
//            actions.add("增加缓存层，减少直接查询ES的次数");
//            actions.add("考虑增加节点或分片，分散搜索压力");
//            actions.add("检查是否有重复或冗余的查询可以合并");
//            suggestion.setActions(actions);
//            suggestions.add(suggestion);
//        }
//
//        if (blockedThreadCount > 3) {
//            ThreadPoolSuggestion suggestion = new ThreadPoolSuggestion();
//            suggestion.setTitle("线程阻塞问题");
//            suggestion.setContent("当前存在较多阻塞线程，可能存在死锁或资源竞争问题。");
//            List<String> actions = new ArrayList<>();
//            actions.add("分析阻塞线程的堆栈信息，找出阻塞原因");
//            actions.add("检查是否存在死锁情况，必要时重启服务");
//            actions.add("优化代码中的锁使用，减少锁的粒度和持有时间");
//            actions.add("增加关键资源的并发访问能力");
//            suggestion.setActions(actions);
//            suggestions.add(suggestion);
//        }
//    }
//
//    // ===================================================================================
//
//    /**
//     * 新增：检查线程池统计信息（模拟从 /cat/thread_pool 获取数据）
//     *
//     * @param result      检测结果对象
//     * @param checkItems  检测项集合
//     * @param suggestions 优化建议集合
//     */
//    private void checkThreadPoolStats(ThreadCheckResult result,
//                                      List<ThreadCheckItem> checkItems,
//                                      List<ThreadPoolSuggestion> suggestions) {
//        // 模拟从外部接口获取线程池信息
////        List<ThreadPoolStat> poolStats = elasticClientService.fetchThreadPoolStats();
////
////        for (ThreadPoolStat stat : poolStats) {
////            // 检查队列大小
////            ThreadCheckItem queueItem = new ThreadCheckItem();
////            queueItem.setCategory("线程池[" + stat.getName() + "]");
////            queueItem.setItem("等待队列长度");
////            queueItem.setValue(String.valueOf(stat.getQueue()));
////            queueItem.setThreshold("< 100");
////
////            if (stat.getQueue() > 200) {
////                queueItem.setStatus("严重");
////                queueItem.setDescription("线程池队列积压严重，可能影响任务响应");
////                result.setOverallStatus("异常");
////            } else if (stat.getQueue() > 100) {
////                queueItem.setStatus("警告");
////                queueItem.setDescription("线程池队列较多，需关注任务执行情况");
////                if (!"异常".equals(result.getOverallStatus())) {
////                    result.setOverallStatus("警告");
////                }
////            } else {
////                queueItem.setStatus("正常");
////                queueItem.setDescription("线程池队列长度正常");
////            }
////            checkItems.add(queueItem);
////
////            // 检查活跃线程数
////            ThreadCheckItem activeItem = new ThreadCheckItem();
////            activeItem.setCategory("线程池[" + stat.getName() + "]");
////            activeItem.setItem("活跃线程数");
////            activeItem.setValue(String.valueOf(stat.getActive()));
////            activeItem.setThreshold("< 80% of max");
////
////            int maxThreads = stat.getSize(); // 假设 size 是最大线程数
////            double activeRatio = ((double) stat.getActive() / maxThreads) * 100;
////
////            if (activeRatio > 95) {
////                activeItem.setStatus("严重");
////                activeItem.setDescription("线程池几乎满负荷运行，可能导致任务延迟");
////                result.setOverallStatus("异常");
////            } else if (activeRatio > 80) {
////                activeItem.setStatus("警告");
////                activeItem.setDescription("线程池负载较高，需关注");
////                if (!"异常".equals(result.getOverallStatus())) {
////                    result.setOverallStatus("警告");
////                }
////            } else {
////                activeItem.setStatus("正常");
////                activeItem.setDescription("线程池负载正常");
////            }
////            checkItems.add(activeItem);
////
////            // 生成建议
////            if (stat.getQueue() > 100) {
////                ThreadSuggestion suggestion = new ThreadSuggestion();
////                suggestion.setTitle("线程池[" + stat.getName() + "] 队列积压");
////                suggestion.setContent("当前线程池队列长度较大，可能存在性能瓶颈。");
////                List<String> actions = new ArrayList<>();
////                actions.add("增加线程池核心线程数或最大线程数");
////                actions.add("优化任务处理逻辑，减少单个任务耗时");
////                actions.add("考虑将部分任务异步化或限流");
////                suggestion.setActions(actions);
////                suggestions.add(suggestion);
////            }
////
////            if (activeRatio > 80) {
////                ThreadSuggestion suggestion = new ThreadSuggestion();
////                suggestion.setTitle("线程池[" + stat.getName() + "] 负载过高");
////                suggestion.setContent("线程池活跃线程比例较高，系统资源可能紧张。");
////                List<String> actions = new ArrayList<>();
////                actions.add("调整线程池配置，适当增加最大线程数");
////                actions.add("分析任务来源，优化高频任务");
////                actions.add("考虑部署更多节点来分担负载");
////                suggestion.setActions(actions);
////                suggestions.add(suggestion);
////            }
////        }
//    }
//}