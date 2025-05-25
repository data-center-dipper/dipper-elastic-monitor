package com.dipper.monitor.service.elastic.thread.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadPoolStat;
import com.dipper.monitor.entity.elastic.thread.check.yanshi.ThreadPoolItem;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.thread.ThreadPoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ThreadPoolServiceImpl implements ThreadPoolService {

    @Autowired
    private ElasticClientService elasticClientService;

    @Override
    public List<ThreadPoolItem> fetchThreadPool() throws IOException {
        String response = elasticClientService.executeGetApi("/_cat/thread_pool?format=json"); // 返回 JSON 字符串
        if (response == null || response.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<ThreadPoolItem> itemList = new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(response);

        for (Object obj : jsonArray) {
            JSONObject node = (JSONObject) obj;

            // 提取每个线程池信息
            String nodeName = node.getString("node_name");
            String name = node.getString("name");
            Integer active = node.getInteger("active");
            Integer queue = node.getInteger("queue");
            Integer rejected = node.getInteger("rejected");

            ThreadPoolItem threadPoolItem = new ThreadPoolItem();
            threadPoolItem.setNodeName(nodeName);
            threadPoolItem.setName(name);
            threadPoolItem.setActive(active);
            threadPoolItem.setQueue(queue);
            threadPoolItem.setRejected(rejected);
            itemList.add(threadPoolItem);
        }
        return itemList;
    }

    @Override
    /**
     * 获取所有节点的线程池状态信息
     */
    public List<ThreadPoolStat> fetchThreadPoolStats() {
        try {
            List<ThreadPoolItem> response = fetchThreadPool();
            return parseThreadPoolStats(response);
        } catch (IOException e) {
            System.err.println("请求或解析线程池信息失败：" + e.getMessage());
            return Collections.emptyList();
        }
    }



    /**
     * 解析 _cat/thread_pool 返回的文本格式数据
     */
    public List<ThreadPoolStat> parseThreadPoolStats(List<ThreadPoolItem> content) {
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }

        // 定义线程池默认最大线程数
        Map<String, Integer> threadPoolDefaultSize = Map.of(
                "bulk", 4,
                "index", 4,
                "search", 8,
                "snapshot", 2,
                "generic", 30,
                "warmer", 1,
                "flush", 1,
                "management", 5,
                "listener", 3,
                "force_merge", 1
        );

        // 按线程池名称分组
        Map<String, List<ThreadPoolItem>> groupedByPoolName = content.stream()
                .collect(Collectors.groupingBy(ThreadPoolItem::getName));

        List<ThreadPoolStat> stats = new ArrayList<>();

        for (Map.Entry<String, List<ThreadPoolItem>> entry : groupedByPoolName.entrySet()) {
            String poolName = entry.getKey();
            List<ThreadPoolItem> items = entry.getValue();

            int totalActive = items.stream().mapToInt(item -> item.getActive() == null ? 0 : item.getActive()).sum();
            int totalQueue = items.stream().mapToInt(item -> item.getQueue() == null ? 0 : item.getQueue()).sum();

            // 使用默认线程池大小
            int size = threadPoolDefaultSize.getOrDefault(poolName, 4); // 如果找不到，默认是4

            ThreadPoolStat stat = new ThreadPoolStat();
            stat.setName(poolName);
            stat.setActive(totalActive);
            stat.setQueue(totalQueue);
            stat.setSize(size);

            stats.add(stat);
        }

        return stats;
    }


}
