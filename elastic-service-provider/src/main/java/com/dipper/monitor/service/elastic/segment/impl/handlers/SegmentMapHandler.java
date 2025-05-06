package com.dipper.monitor.service.elastic.segment.impl.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.segments.SegmentMessage;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.*;

@Slf4j
public class SegmentMapHandler {

    private final ElasticClientService elasticClientService;

    public SegmentMapHandler(ElasticClientService elasticClientService) {
        this.elasticClientService = elasticClientService;
    }

    public Map<String, List<SegmentMessage>> segmentMap() {
        String result;
        try {
            result = elasticClientService.executeGetApi(ElasticRestApi.SEGMENT_LIST.getApiPath());
        } catch (Exception e) {
            log.error("获取segment信息异常：{}", e.getMessage(), e);
            return Collections.emptyMap();
        }

        JSONArray jsonArray = JSON.parseArray(result);
        Map<String, List<SegmentMessage>> map = new HashMap<>();

        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            String index = jsonObject.getString("index");
            Integer shard = jsonObject.getInteger("shard");
            String prirep = jsonObject.getString("prirep");
            String ip = jsonObject.getString("ip");
            String segment = jsonObject.getString("segment");
            Long generation = jsonObject.getLong("generation");
            Long docsCount = jsonObject.getLong("docs.count");
            Long docsDeleted = jsonObject.getLong("docs.deleted");
            String size = jsonObject.getString("size");
            Long sizeMemory = jsonObject.getLong("size.memory");
            boolean committed = jsonObject.getBoolean("committed");
            boolean searchable = jsonObject.getBoolean("searchable");
            String version = jsonObject.getString("version");
            boolean compound = jsonObject.getBoolean("compound");

            SegmentMessage segmentMessage = new SegmentMessage(index, shard, prirep, ip,
                    segment, generation, docsCount, docsDeleted,
                    size, sizeMemory, committed, searchable, version, compound);

            map.computeIfAbsent(index, k -> new ArrayList<>()).add(segmentMessage);
        }

        return map;
    }
}