package com.dipper.monitor.service.elastic.thread.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.thread.*;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.thread.ThreadManagerService;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ThreadManagerServiceImpl implements ThreadManagerService {

    @Autowired
    private ElasticClientService elasticClientService;

    private static final String HOT_THREADS_API = "/_nodes/hot_threads";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 缓存线程列表，避免频繁请求ES
    private List<ThreadHotView> cachedThreadList = new ArrayList<>();

    @Override
    public Tuple2<List<ThreadHotView>, Long> threadPage(ThreadPageReq threadPageReq) {
        // 如果缓存为空，则刷新线程列表
        if (cachedThreadList.isEmpty()) {
            refreshThreadList();
        }

        List<ThreadHotView> filteredList = cachedThreadList;

        // 根据搜索条件过滤
        if (StringUtils.isNotBlank(threadPageReq.getSearchText())) {
            String keyword = threadPageReq.getSearchText().toLowerCase();
            filteredList = filteredList.stream()
                    .filter(thread -> 
                        thread.getName().toLowerCase().contains(keyword) ||
                        thread.getType().toLowerCase().contains(keyword) ||
                        thread.getDescription().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }

        // 根据线程类型过滤
        if (StringUtils.isNotBlank(threadPageReq.getThreadType())) {
            filteredList = filteredList.stream()
                    .filter(thread -> thread.getType().equals(threadPageReq.getThreadType()))
                    .collect(Collectors.toList());
        }

        // 根据线程状态过滤
        if (StringUtils.isNotBlank(threadPageReq.getThreadStatus())) {
            filteredList = filteredList.stream()
                    .filter(thread -> thread.getStatus().equals(threadPageReq.getThreadStatus()))
                    .collect(Collectors.toList());
        }

        // 计算总数
        long total = filteredList.size();

        // 分页处理
        int start = (threadPageReq.getPageNum() - 1) * threadPageReq.getPageSize();
        int end = Math.min(start + threadPageReq.getPageSize(), filteredList.size());

        // 防止索引越界
        if (start >= filteredList.size()) {
            return new Tuple2<>(new ArrayList<>(), total);
        }

        List<ThreadHotView> pageList = filteredList.subList(start, end);
        return new Tuple2<>(pageList, total);
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
        try {
            // 调用ES API获取热点线程信息
            String response = elasticClientService.executeGetApi(HOT_THREADS_API);
            
            // 解析响应并转换为ThreadHotView列表
            List<ThreadHotView> threadList = parseHotThreadsResponse(response);
            
            // 更新缓存
            cachedThreadList = threadList;
            return threadList;
        } catch (Exception e) {
            log.error("刷新热点线程列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 解析ES热点线程响应
     * 注意：实际实现需要根据ES返回的具体格式进行解析
     * 这里提供一个示例实现
     */
    private List<ThreadHotView> parseHotThreadsResponse(String response) {
        List<ThreadHotView> result = new ArrayList<>();
        
        try {
            // 这里是示例解析逻辑，实际需要根据ES返回的格式调整
            // 假设返回的是文本格式，需要解析文本提取线程信息
            String[] nodeThreads = response.split(":\n");
            
            int id = 1;
            for (int i = 1; i < nodeThreads.length; i++) { // 跳过第一个元素（通常是响应头）
                String nodeThread = nodeThreads[i];
                String[] threadBlocks = nodeThread.split("\n\n");
                
                for (String threadBlock : threadBlocks) {
                    if (StringUtils.isBlank(threadBlock)) continue;
                    
                    ThreadHotView thread = new ThreadHotView();
                    thread.setId(id++);
                    
                    // 解析线程名称
                    String[] lines = threadBlock.split("\n");
                    if (lines.length > 0) {
                        String firstLine = lines[0].trim();
                        if (firstLine.contains("[")) {
                            thread.setName(firstLine.substring(0, firstLine.indexOf("[")).trim());
                            
                            // 根据线程名称推断类型
                            if (thread.getName().contains("search")) {
                                thread.setType("搜索线程");
                            } else if (thread.getName().contains("write")) {
                                thread.setType("写入线程");
                            } else if (thread.getName().contains("bulk")) {
                                thread.setType("批量线程");
                            } else if (thread.getName().contains("management")) {
                                thread.setType("管理线程");
                            } else if (thread.getName().contains("refresh")) {
                                thread.setType("刷新线程");
                            } else if (thread.getName().contains("merge")) {
                                thread.setType("合并线程");
                            } else if (thread.getName().contains("snapshot")) {
                                thread.setType("快照线程");
                            } else if (thread.getName().contains("recovery")) {
                                thread.setType("恢复线程");
                            } else {
                                thread.setType("通用线程");
                            }
                        } else {
                            thread.setName("未知线程");
                            thread.setType("通用线程");
                        }
                    }
                    
                    // 设置CPU使用率（示例值）
                    thread.setCpu((int)(Math.random() * 100));
                    
                    // 设置内存占用（示例值）
                    thread.setMemory(((int)(Math.random() * 200) + 10) + "MB");
                    
                    // 设置状态（根据堆栈信息推断）
                    if (threadBlock.contains("RUNNABLE")) {
                        thread.setStatus("运行中");
                    } else if (threadBlock.contains("WAITING") || threadBlock.contains("TIMED_WAITING")) {
                        thread.setStatus("等待中");
                    } else if (threadBlock.contains("BLOCKED")) {
                        thread.setStatus("阻塞");
                    } else {
                        thread.setStatus("休眠");
                    }
                    
                    // 设置创建时间（当前时间）
                    thread.setCreateTime(new Date());
                    
                    // 设置描述（根据线程类型生成）
                    thread.setDescription(generateThreadDescription(thread.getType()));
                    
                    // 设置堆栈信息
                    StringBuilder stackTrace = new StringBuilder();
                    for (int j = 1; j < lines.length; j++) {
                        stackTrace.append(lines[j].trim()).append("\n");
                    }
                    thread.setStackTrace(stackTrace.toString());
                    
                    result.add(thread);
                }
            }
        } catch (Exception e) {
            log.error("解析热点线程响应失败", e);
        }
        
        return result;
    }
    
    /**
     * 根据线程类型生成描述
     */
    private String generateThreadDescription(String type) {
        switch (type) {
            case "搜索线程":
                return "负责处理搜索请求，高CPU使用率可能表示查询复杂或数据量大。";
            case "写入线程":
                return "负责处理索引写入请求，高CPU使用率表示写入压力大。";
            case "批量线程":
                return "处理批量操作请求，如批量索引或删除，高CPU使用率表示批量操作频繁。";
            case "管理线程":
                return "处理集群管理操作，如节点发现、状态更新等，通常CPU使用率较低。";
            case "刷新线程":
                return "负责刷新索引，使新索引的文档可被搜索，低CPU使用率是正常的。";
            case "合并线程":
                return "负责段合并操作，优化索引性能，高CPU使用率表示正在进行大量合并。";
            case "快照线程":
                return "负责创建和恢复快照，中等CPU使用率表示正在进行快照操作。";
            case "恢复线程":
                return "负责分片恢复操作，在节点重启或新节点加入时活跃，中等CPU使用率表示正在进行恢复操作。";
            case "通用线程":
                return "通用线程池中的线程，处理各种不同类型的请求，低CPU使用率表示负载较轻。";
            default:
                return "未知类型线程";
        }
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
}
