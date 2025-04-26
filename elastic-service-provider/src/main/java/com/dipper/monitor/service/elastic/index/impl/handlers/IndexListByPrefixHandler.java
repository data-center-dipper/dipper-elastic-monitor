package com.dipper.monitor.service.elastic.index.impl.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class IndexListByPrefixHandler extends AbstractIndexHandler {

    public IndexListByPrefixHandler(ElasticClientService elasticClientService) {
        super(elasticClientService);
    }

    public List<IndexEntity> listIndexNameByPrefix(String indexPrefix, String indexXing) throws IOException {
        String api = "/_cat/indices/" + indexXing + "?format=json";
        log.info("获取某种类型的索引：{}", api);
        String res1 = this.elasticClientService.executeGetApi(api);
        JSONArray jsonArray = JSON.parseArray(res1);

        Map<String, IndexEntity> map = new HashMap<>();
        jsonArray.forEach(jsonObject -> {
            JSONObject obj = (JSONObject) jsonObject;
            try {
                IndexEntity indexEntity = convertToIndexEntity(obj, false, false);
                map.put(obj.getString("index"), indexEntity);
            } catch (IOException e) {
                log.error("处理索引数据时发生错误", e);
            }
        });

        // 过滤并排序索引实体
        List<IndexEntity> filteredAndSortedIndices = map.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(indexPrefix))
                .map(Map.Entry::getValue)
                .sorted((e1, e2) -> e2.getIndex().compareTo(e1.getIndex())) // 基于索引名称降序排列
                .collect(Collectors.toList());

        log.info("获取前缀为 {} 的索引个数总结果：{}", indexPrefix, filteredAndSortedIndices.size());
        return filteredAndSortedIndices;
    }

}