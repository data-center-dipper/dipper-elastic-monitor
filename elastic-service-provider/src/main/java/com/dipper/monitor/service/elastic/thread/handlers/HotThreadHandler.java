package com.dipper.monitor.service.elastic.thread.handlers;

import com.dipper.monitor.entity.elastic.thread.ThreadHotView;
import com.dipper.monitor.entity.elastic.thread.hot.HotThreadMeta;
import com.dipper.monitor.entity.elastic.thread.hot.HotThreadMiddle;
import com.dipper.monitor.entity.elastic.thread.hot.NodeMetadata;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.utils.Tuple2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HotThreadHandler {


    private static final String HOT_THREADS_API = "/_nodes/hot_threads?format=json";

    private ElasticClientService elasticClientService;

    public HotThreadHandler(ElasticClientService elasticClientService) {
        this.elasticClientService = elasticClientService;
    }

    /**
     * 刷新热点线程列表
     */
    public List<ThreadHotView> refreshThreadList() {
        try {
            String response = elasticClientService.executeGetApi(HOT_THREADS_API);
            return parseHotThreadsResponse(response);
        } catch (Exception e) {
            System.err.println("刷新热点线程列表失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 解析 Elasticsearch 返回的 _nodes/hot_threads 接口数据
     */
    public List<ThreadHotView> parseHotThreadsResponse(String response) {
        List<ThreadHotView> result = new ArrayList<>();

        if (StringUtils.isBlank(response)) {
            return result;
        }

        // Step 1: 分割每个节点的数据块（按 ::: 分隔）
        String[] nodeSections = response.split(":::");

        for (String section : nodeSections) {
            if (StringUtils.isBlank(section)) continue;

            // Step 2: 提取该节点的元数据和热点线程描述
            HotThreadMiddle hotThreadMiddle = processOneSelection(section);

            NodeMetadata nodeMetadata = hotThreadMiddle.getNodeMetadata();
            List<HotThreadMeta> hotThreadMetas = hotThreadMiddle.getHotThreadMetas();

            // Step 3: 提取该节点下的所有线程详情
            ThreadHotView view = buildThreadHotView(nodeMetadata,hotThreadMetas);
            view.setDetail(section);
            result.add(view);
        }

        return result;
    }

    /**
     * 处理一个节点的数据块（包括元数据和线程信息）
     */
    public HotThreadMiddle processOneSelection(String section) {
        String[] lines = section.trim().split("\n");

        if (lines.length < 2) {
            return new HotThreadMiddle();
        }

        String nodeMedata = lines[0];              // 节点元数据行
        String hotThreadsLine = lines[1];          // 热点线程描述行

        NodeMetadata metadata = processNodeMetadata(nodeMedata);
        HotThreadMeta threadMeta = processOneHotThread(hotThreadsLine);

        HotThreadMiddle middle = new HotThreadMiddle();
        middle.setNodeMetadata(metadata);
        middle.setHotThreadMetas(Collections.singletonList(threadMeta));

        return middle;
    }

    /**
     * 提取每一行 {} 里面的信息并转为 NodeMetadata 对象
     */
    public NodeMetadata processNodeMetadata(String nodeMedata) {
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(nodeMedata);

        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group(1));
        }

        if (list.size() < 7) {
            throw new IllegalArgumentException("输入格式不正确，缺少必要字段");
        }

        NodeMetadata metadata = new NodeMetadata();
        metadata.setNodeIdShort(list.get(0));         // 1.es3
        metadata.setNodeId(list.get(1));              // tEq2FE4oTwmBptZ5FYQm8w
        metadata.setNodeGuid(list.get(2));            // eEgDpFa4SOiyYuyJKuTbvg
        metadata.setIp(list.get(3));                  // 78.118.1.34
        metadata.setHostAndPort(list.get(4));         // 78.118.1.34:9300
        metadata.setRoles(list.get(5));               // cdfhimrstw
        metadata.setAttributes(list.get(6));          // xpack.installed=true, transform.node=true

        return metadata;
    }

    /**
     * 解析热点线程描述行，如：
     * Hot threads at 2025-05-24T06:28:59.029Z, interval=500ms, busiestThreads=3, ignoreIdleThreads=true:
     */
    private HotThreadMeta processOneHotThread(String hotThread) {
        hotThread = hotThread.replace("Hot threads at ", "").trim();

        String[] parts = hotThread.split(",");
        String timestamp = parts[0];

        Map<String, String> configMap = new HashMap<>();
        for (int i = 1; i < parts.length; i++) {
            String[] kv = parts[i].trim().split("=", 2);
            if (kv.length == 2) {
                configMap.put(kv[0], kv[1]);
            }
        }

        return new HotThreadMeta(timestamp, configMap);
    }

    /**
     * 构造 ThreadHotView 实例
     */
    private ThreadHotView buildThreadHotView(NodeMetadata metadata,
                                             List<HotThreadMeta> hotThreadMetas) {


        String dateTime = getDateTime(hotThreadMetas);

        String nodeIdShort= metadata.getNodeIdShort();
        String nodeId= metadata.getNodeId();

        ThreadHotView thread = new ThreadHotView();
        BeanUtils.copyProperties(metadata, thread);
        thread.setId(nodeId);
        thread.setCreateTime(dateTime);
        thread.setName(nodeIdShort);
        thread.setType(inferTypeFromName("xx"));
        thread.setHotThreadMetas(hotThreadMetas);

        return thread;
    }

    private String getDateTime(List<HotThreadMeta> hotThreadMetas) {
        if( hotThreadMetas.size() > 0) {
            String dateTime = hotThreadMetas.get(0).getDateTime();
            return dateTime;
        }
        return "-";
    }

    /**
     * 根据线程名推断类型
     */
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

    /**
     * 根据线程类型生成描述
     */
    private String generateThreadDescription(String type) {
        switch (type) {
            case "搜索线程":
                return "负责处理搜索请求，高CPU使用率可能表示查询复杂或数据量大。";
            case "写入线程":
                return "负责处理索引写入请求，高CPU使用率表示写入压力大。";
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
}