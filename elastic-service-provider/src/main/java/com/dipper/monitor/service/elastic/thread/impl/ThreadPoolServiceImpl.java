package com.dipper.monitor.service.elastic.thread.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.thread.check.ThreadPoolStat;
import com.dipper.monitor.entity.elastic.thread.check.yanshi.ThreadPoolItem;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.thread.ThreadPoolService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

}
