package com.dipper.monitor.service.elastic.thread.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.thread.*;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadCharReq;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadChartSummary;
import com.dipper.monitor.mapper.ElasticThreadMetricMapper;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.thread.ThreadManagerService;
import com.dipper.monitor.service.elastic.thread.handlers.HotThreadHandler;
import com.dipper.monitor.service.elastic.thread.handlers.ThreadChartSummaryHandler;
import com.dipper.monitor.utils.Tuple2;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ThreadManagerServiceImpl implements ThreadManagerService {

    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticThreadMetricMapper elasticThreadMetricMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    // 缓存线程列表，避免频繁请求ES
    private List<ThreadHotView> cachedThreadList = new ArrayList<>();

    @Override
    public List<ThreadHotView> threadPage() {
        // 如果缓存为空，则刷新线程列表
        if (cachedThreadList.isEmpty()) {
            cachedThreadList = refreshThreadList();
        }
        return cachedThreadList;
    }

    @Override
    public ThreadHotView getThreadDetail(Integer threadId) {
        // 从缓存中查找线程
        return cachedThreadList.stream()
                .filter(thread -> thread.getId().equals(threadId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ThreadHotView> refreshThreadList() {
        HotThreadHandler handler = new HotThreadHandler(elasticClientService);
        return handler.refreshThreadList();

    }



    @Override
    public void saveThreadMetrics(List<ThreadMetricEntity> metrics) {
        try {
            if (metrics == null || metrics.isEmpty()) {
                log.warn("没有线程池指标数据需要保存");
                return;
            }

            elasticThreadMetricMapper.batchInsert(metrics);
            log.info("成功保存 {} 条线程池指标数据", metrics.size());
        } catch (Exception e) {
            log.error("保存线程池指标数据失败", e);
            throw e;
        }
    }

    @Override
    public void cleanHistoryData(int retentionDays) {
        try {
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(retentionDays);
            int deletedCount = elasticThreadMetricMapper.deleteByCollectTimeBefore(beforeTime);
            log.info("成功清理 {} 条历史线程池指标数据（保留 {} 天）", deletedCount, retentionDays);
        } catch (Exception e) {
            log.error("清理历史线程池指标数据失败", e);
        }
    }

    @Override
    public List<ThreadMetricEntity> getThreadMetricsByClusterAndNode(String clusterCode, String nodeName,
                                                                     String threadType, LocalDateTime startTime,
                                                                     LocalDateTime endTime) {
        return elasticThreadMetricMapper.selectByClusterNodeAndType(clusterCode, nodeName, threadType, startTime, endTime);
    }

    @Override
    public List<ThreadMetricEntity> getThreadMetricsByClusterAndType(String clusterCode, String threadType,
                                                                     LocalDateTime startTime, LocalDateTime endTime) {
        return elasticThreadMetricMapper.selectByClusterAndType(clusterCode, threadType, startTime, endTime);
    }

    @Override
    public ThreadMetricEntity getLatestThreadMetric(String clusterCode, String nodeName, String threadType) {
        return elasticThreadMetricMapper.selectLatestByNode(clusterCode, nodeName, threadType);
    }


    @Override
    public ThreadCheckResult checkThreadEnvironment() {
        // 确保线程列表是最新的
        if (cachedThreadList.isEmpty()) {
            refreshThreadList();
        }
        
        ThreadCheckResult result = new ThreadCheckResult();
        List<ThreadCheckItem> checkItems = new ArrayList<>();
        List<ThreadSuggestion> suggestions = new ArrayList<>();
        
        // 默认状态为正常
        result.setOverallStatus("正常");
        result.setReadStatus("正常");
        result.setWriteStatus("正常");
        
        // 统计各类线程数量和状态
        int searchThreadCount = 0;
        int writeThreadCount = 0;
        int batchThreadCount = 0;
        int managementThreadCount = 0;
        int highCpuThreadCount = 0;
        int blockedThreadCount = 0;
        
        for (ThreadHotView thread : cachedThreadList) {
            // 统计各类型线程
            switch (thread.getType()) {
                case "搜索线程":
                    searchThreadCount++;
                    break;
                case "写入线程":
                    writeThreadCount++;
                    break;
                case "批量线程":
                    batchThreadCount++;
                    break;
                case "管理线程":
                    managementThreadCount++;
                    break;
                default:
                    break;
            }
            
            // 统计高CPU使用率线程
//            if (thread.getCpu() != null && thread.getCpu().contains("%")) {
//                String cpuStr = thread.getCpu().replace("%", "");
//                try {
//                    double cpuUsage = Double.parseDouble(cpuStr);
//                    if (cpuUsage > 80) {
//                        highCpuThreadCount++;
//                    }
//                } catch (NumberFormatException e) {
//                    log.warn("解析CPU使用率失败: {}", thread.getCpuUsage());
//                }
//            }
            
            // 统计阻塞线程
            if (thread.getStackTrace() != null && 
                (thread.getStackTrace().contains("BLOCKED") || 
                 thread.getStackTrace().contains("WAITING") || 
                 thread.getStackTrace().contains("TIMED_WAITING"))) {
                blockedThreadCount++;
            }
        }
        
        // 添加检测项 - 搜索线程
        ThreadCheckItem searchThreadItem = new ThreadCheckItem();
        searchThreadItem.setCategory("线程数量");
        searchThreadItem.setItem("搜索线程数");
        searchThreadItem.setValue(String.valueOf(searchThreadCount));
        searchThreadItem.setThreshold("< 50");
        if (searchThreadCount > 100) {
            searchThreadItem.setStatus("严重");
            searchThreadItem.setDescription("搜索线程数量过多，可能导致资源竞争");
            result.setReadStatus("压力过大");
            result.setOverallStatus("异常");
        } else if (searchThreadCount > 50) {
            searchThreadItem.setStatus("警告");
            searchThreadItem.setDescription("搜索线程数量较多，需要关注");
            result.setReadStatus("压力较大");
            if (!"异常".equals(result.getOverallStatus())) {
                result.setOverallStatus("警告");
            }
        } else {
            searchThreadItem.setStatus("正常");
            searchThreadItem.setDescription("搜索线程数量正常");
        }
        checkItems.add(searchThreadItem);
        
        // 添加检测项 - 写入线程
        ThreadCheckItem writeThreadItem = new ThreadCheckItem();
        writeThreadItem.setCategory("线程数量");
        writeThreadItem.setItem("写入线程数");
        writeThreadItem.setValue(String.valueOf(writeThreadCount));
        writeThreadItem.setThreshold("< 30");
        if (writeThreadCount > 60) {
            writeThreadItem.setStatus("严重");
            writeThreadItem.setDescription("写入线程数量过多，可能导致资源竞争");
            result.setWriteStatus("压力过大");
            result.setOverallStatus("异常");
        } else if (writeThreadCount > 30) {
            writeThreadItem.setStatus("警告");
            writeThreadItem.setDescription("写入线程数量较多，需要关注");
            result.setWriteStatus("压力较大");
            if (!"异常".equals(result.getOverallStatus())) {
                result.setOverallStatus("警告");
            }
        } else {
            writeThreadItem.setStatus("正常");
            writeThreadItem.setDescription("写入线程数量正常");
        }
        checkItems.add(writeThreadItem);
        
        // 添加检测项 - 批量线程
        ThreadCheckItem batchThreadItem = new ThreadCheckItem();
        batchThreadItem.setCategory("线程数量");
        batchThreadItem.setItem("批量线程数");
        batchThreadItem.setValue(String.valueOf(batchThreadCount));
        batchThreadItem.setThreshold("< 20");
        if (batchThreadCount > 40) {
            batchThreadItem.setStatus("严重");
            batchThreadItem.setDescription("批量线程数量过多，可能导致资源竞争");
            result.setOverallStatus("异常");
        } else if (batchThreadCount > 20) {
            batchThreadItem.setStatus("警告");
            batchThreadItem.setDescription("批量线程数量较多，需要关注");
            if (!"异常".equals(result.getOverallStatus())) {
                result.setOverallStatus("警告");
            }
        } else {
            batchThreadItem.setStatus("正常");
            batchThreadItem.setDescription("批量线程数量正常");
        }
        checkItems.add(batchThreadItem);
        
        // 添加检测项 - 管理线程
        ThreadCheckItem managementThreadItem = new ThreadCheckItem();
        managementThreadItem.setCategory("线程数量");
        managementThreadItem.setItem("管理线程数");
        managementThreadItem.setValue(String.valueOf(managementThreadCount));
        managementThreadItem.setThreshold("< 10");
        if (managementThreadCount > 20) {
            managementThreadItem.setStatus("警告");
            managementThreadItem.setDescription("管理线程数量较多，需要关注");
            if (!"异常".equals(result.getOverallStatus())) {
                result.setOverallStatus("警告");
            }
        } else {
            managementThreadItem.setStatus("正常");
            managementThreadItem.setDescription("管理线程数量正常");
        }
        checkItems.add(managementThreadItem);
        
        // 添加检测项 - 高CPU线程
        ThreadCheckItem highCpuItem = new ThreadCheckItem();
        highCpuItem.setCategory("系统资源");
        highCpuItem.setItem("高CPU使用率线程数");
        highCpuItem.setValue(String.valueOf(highCpuThreadCount));
        highCpuItem.setThreshold("< 5");
        if (highCpuThreadCount > 10) {
            highCpuItem.setStatus("严重");
            highCpuItem.setDescription("高CPU使用率线程数量过多，系统负载过高");
            result.setOverallStatus("异常");
        } else if (highCpuThreadCount > 5) {
            highCpuItem.setStatus("警告");
            highCpuItem.setDescription("高CPU使用率线程数量较多，系统负载较高");
            if (!"异常".equals(result.getOverallStatus())) {
                result.setOverallStatus("警告");
            }
        } else {
            highCpuItem.setStatus("正常");
            highCpuItem.setDescription("高CPU使用率线程数量正常");
        }
        checkItems.add(highCpuItem);
        
        // 添加检测项 - 阻塞线程
        ThreadCheckItem blockedItem = new ThreadCheckItem();
        blockedItem.setCategory("线程阻塞");
        blockedItem.setItem("阻塞线程数");
        blockedItem.setValue(String.valueOf(blockedThreadCount));
        blockedItem.setThreshold("< 3");
        if (blockedThreadCount > 5) {
            blockedItem.setStatus("严重");
            blockedItem.setDescription("阻塞线程数量过多，可能存在死锁或资源竞争");
            result.setOverallStatus("异常");
        } else if (blockedThreadCount > 3) {
            blockedItem.setStatus("警告");
            blockedItem.setDescription("阻塞线程数量较多，需要关注");
            if (!"异常".equals(result.getOverallStatus())) {
                result.setOverallStatus("警告");
            }
        } else {
            blockedItem.setStatus("正常");
            blockedItem.setDescription("阻塞线程数量正常");
        }
        checkItems.add(blockedItem);
        
        // 生成优化建议
        if (batchThreadCount > 20) {
            ThreadSuggestion batchSuggestion = new ThreadSuggestion();
            batchSuggestion.setTitle("批量线程压力过大");
            batchSuggestion.setContent("当前批量线程数量较多，可能导致系统资源竞争，影响整体性能。");
            List<String> actions = new ArrayList<>();
            actions.add("调整批量操作的大小和频率，避免同时执行过多批量操作");
            actions.add("增加批量线程池的大小，但需要确保系统有足够的资源");
            actions.add("检查是否有不必要的批量操作，可以合并或取消");
            batchSuggestion.setActions(actions);
            suggestions.add(batchSuggestion);
        }
        
        if (searchThreadCount > 50) {
            ThreadSuggestion searchSuggestion = new ThreadSuggestion();
            searchSuggestion.setTitle("搜索性能下降");
            searchSuggestion.setContent("当前搜索线程数量较多，可能导致搜索性能下降。");
            List<String> actions = new ArrayList<>();
            actions.add("优化复杂查询，减少不必要的字段和过滤条件");
            actions.add("增加缓存层，减少直接查询ES的次数");
            actions.add("考虑增加节点或分片，分散搜索压力");
            actions.add("检查是否有重复或冗余的查询可以合并");
            searchSuggestion.setActions(actions);
            suggestions.add(searchSuggestion);
        }
        
        if (blockedThreadCount > 3) {
            ThreadSuggestion blockedSuggestion = new ThreadSuggestion();
            blockedSuggestion.setTitle("线程阻塞问题");
            blockedSuggestion.setContent("当前存在较多阻塞线程，可能存在死锁或资源竞争问题。");
            List<String> actions = new ArrayList<>();
            actions.add("分析阻塞线程的堆栈信息，找出阻塞原因");
            actions.add("检查是否存在死锁情况，必要时重启服务");
            actions.add("优化代码中的锁使用，减少锁的粒度和持有时间");
            actions.add("增加关键资源的并发访问能力，如使用并发数据结构");
            blockedSuggestion.setActions(actions);
            suggestions.add(blockedSuggestion);
        }
        
        result.setCheckItems(checkItems);
        result.setSuggestions(suggestions);
        
        return result;
    }

    @Override
    public List<ThreadMetricEntity> getThreadMetrics(ThreadCharReq threadCharReq) {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        threadCharReq.setClusterCode(clusterCode);
        return elasticThreadMetricMapper.getThreadMetrics(threadCharReq);
    }

    @Override
    public List<ThreadChartSummary> threadChartSummary(ThreadCharReq threadCharReq) {
        List<ThreadMetricEntity> threadMetrics = getThreadMetrics(threadCharReq);
        ThreadChartSummaryHandler handler = new ThreadChartSummaryHandler(threadMetrics);
        return handler.threadChartSummary();
    }
}
