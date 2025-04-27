package com.dipper.monitor.service.elastic.index.impl.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.impl.thread.IndexSettingCallable;
import com.dipper.monitor.utils.CommonThreadFactory;
import com.dipper.monitor.utils.DiscardOldestThreadPolicy;
import com.dipper.monitor.utils.ListUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class IndexListPatternThreadHandler extends AbstractIndexHandler {

    private static volatile ThreadPoolExecutor exeLongRequest = new ThreadPoolExecutor(10, 10, 1L, TimeUnit.HOURS,
            new ArrayBlockingQueue<>(100), (ThreadFactory)
            new CommonThreadFactory("esRestClient"),
            (RejectedExecutionHandler)new DiscardOldestThreadPolicy());


    public IndexListPatternThreadHandler(ElasticClientService elasticClientService) {
        super(elasticClientService);
    }

    public Map<String, IndexEntity> listIndexPatternMapThread(boolean setting, String indexPatternPrefix, String indexXing) throws IOException {
        String api = "/_cat/indices/" + indexXing + "?format=json";
        log.info("获取某种类型的索引：{}", api);
        String res1 = elasticClientService.executeGetApi(api);
        JSONArray jsonArray = JSON.parseArray(res1);

        // 过滤符合前缀的索引
        List<JSONObject> filteredList = jsonArray.stream()
                .map(jsonObject -> (JSONObject) jsonObject)
                .filter(jsonObject -> jsonObject.getString("index").startsWith(indexPatternPrefix))
                .collect(Collectors.toList());

        // 将列表分割为多个子列表，每个包含最多200个元素
        List<List<JSONObject>> indexList = ListUtils.splitListBySize(filteredList, 200);
        if (indexList == null) {
            indexList = Collections.emptyList();
        }

        // 提交多线程任务
        List<Future<Map<String, IndexEntity>>> futureList = new ArrayList<>();
        for (List<JSONObject> childList : indexList) {
            log.info("模板 多线程提交获取索引的设置：{}", childList.size());
            Future<Map<String, IndexEntity>> futureTask = exeLongRequest.submit(new IndexSettingCallable(childList, setting, elasticClientService));
            futureList.add(futureTask);
        }

        // 收集所有结果
        Map<String, IndexEntity> allResult = new HashMap<>();
        int taskCount = futureList.size();
        int completedTasks = 0;
        long startTime = System.currentTimeMillis();

        while (completedTasks != taskCount) {
            Iterator<Future<Map<String, IndexEntity>>> iterator = futureList.iterator();
            while (iterator.hasNext()) {
                Future<Map<String, IndexEntity>> futureTask = iterator.next();
                if (futureTask.isDone()) {
                    try {
                        Map<String, IndexEntity> taskResult = futureTask.get();
                        allResult.putAll(taskResult);
                    } catch (Exception e) {
                        log.info("获取listIndexMap多线程任务执行异常：{}", e.getMessage());
                    }
                    completedTasks++;
                }
            }
            long currentTime = System.currentTimeMillis();

            // 如果超过5分钟（300秒），则退出循环
            if ((currentTime - startTime) / 60000L > 5) {
                break;
            }
        }
        return allResult;
    }
}