package com.dipper.monitor.service.elastic.thread.handlers;

import com.dipper.monitor.entity.elastic.thread.ThreadHotView;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HotThreadHandler {

    private static final Pattern THREAD_HEADER_PATTERN = Pattern.compile("\"(.+?)\".*Id=(\\d+).*?(RUNNABLE|WAITING|TIMED_WAITING|BLOCKED|NEW|TERMINATED)");
    private static final String HOT_THREADS_API = "/_nodes/hot_threads?format=json";

    private ElasticClientService elasticClientService;

    public HotThreadHandler(ElasticClientService elasticClientService) {
        this.elasticClientService = elasticClientService;
    }


    public List<ThreadHotView> refreshThreadList() {
        try {
            // 调用ES API获取热点线程信息
            String response = elasticClientService.executeGetApi(HOT_THREADS_API);

            // 解析响应并转换为ThreadHotView列表
            List<ThreadHotView> threadList = parseHotThreadsResponse(response);
            return threadList;
        } catch (Exception e) {
            log.error("刷新热点线程列表失败", e);
            return new ArrayList<>();
        }
    }


    /**
     * 手动解析Elasticsearch返回的文本格式热点线程信息
     */
    public List<ThreadHotView> parseHotThreadsResponse(String response) {
        List<ThreadHotView> result = new ArrayList<>();

        if (StringUtils.isBlank(response)) {
            return result;
        }

        // Step 1: 分割节点部分（可能有多个节点）
        String[] nodeSections = response.split("::: [^{]+\\{[^}]+\\}\\{[^}]+\\}\\{[^}]+\\}\\{[^}]+\\}\\{[^}]+\\}\\{[^}]+\\}\\{[^}]+\\}\\{[^}]*\\}");
        int nodeIdIndex = 0;

        // Step 2: 遍历每个节点下的线程信息
        for (String section : nodeSections) {
            if (StringUtils.isBlank(section)) continue;

            // 提取当前节点ID
            String nodeId = extractNodeId(response, nodeIdIndex++);

            // 按空行分割出每个线程块
            String[] threadBlocks = section.split("\n\\s*\n");
            int id = 1;

            for (String block : threadBlocks) {
                if (StringUtils.isBlank(block)) continue;

                ThreadHotView thread = new ThreadHotView();
                thread.setId(id++);
                thread.setCreateTime(new Date());

                // 解析头部获取线程名称、ID、状态等信息
                Matcher matcher = THREAD_HEADER_PATTERN.matcher(block);
                if (matcher.find()) {
                    String threadName = matcher.group(1);
                    thread.setName(threadName);
                    thread.setType(inferTypeFromName(threadName));
                    thread.setStatus(matcher.group(3)); // 状态：RUNNABLE / WAITING 等
                } else {
                    thread.setName("未知线程");
                    thread.setType("通用线程");
                    thread.setStatus("未知状态");
                }

                // 设置内存和CPU使用率（示例值）
                thread.setMemory("N/A");
                thread.setCpu((int)(Math.random() * 100));

                // 设置描述
                thread.setDescription(generateThreadDescription(thread.getType()));
                thread.setDetail(response);

                // 设置堆栈信息
                StringBuilder stackTrace = new StringBuilder();
                String[] lines = block.split("\n");

                boolean headerSkipped = false;
                for (String line : lines) {
                    if (!headerSkipped && THREAD_HEADER_PATTERN.matcher(line).find()) {
                        headerSkipped = true;
                        continue;
                    }
                    if (headerSkipped) {
                        stackTrace.append(line.trim()).append("\n");
                    }
                }

                thread.setStackTrace(stackTrace.toString());
                thread.setDescription("来自节点：" + nodeId + " - " + thread.getDescription());

                result.add(thread);
            }
        }

        return result;
    }

    // 根据线程名推断类型
    private String inferTypeFromName(String name) {
        if (name.contains("search")) return "搜索线程";
        if (name.contains("bulk") || name.contains("write")) return "写入线程";
        if (name.contains("management")) return "管理线程";
        if (name.contains("refresh")) return "刷新线程";
        if (name.contains("merge")) return "合并线程";
        if (name.contains("snapshot")) return "快照线程";
        if (name.contains("recovery")) return "恢复线程";
        return "通用线程";
    }

    // 生成描述信息
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
            default:
                return "通用线程池中的线程，处理各种不同类型的请求，低CPU使用率表示负载较轻。";
        }
    }

    // 提取节点ID
    private String extractNodeId(String raw, int index) {
        String[] parts = raw.split(":::");
        if (index >= 0 && index < parts.length) {
            String part = parts[index];
            if (part.contains("{")) {
                return part.split("\\{")[1].replace("}", "");
            }
        }
        return "未知节点";
    }
}