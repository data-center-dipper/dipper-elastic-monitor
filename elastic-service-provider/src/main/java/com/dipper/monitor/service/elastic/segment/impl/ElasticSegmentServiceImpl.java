package com.dipper.monitor.service.elastic.segment.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.segments.SegmentMessage;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.segment.ElasticSegmentService;
import com.dipper.monitor.service.elastic.segment.impl.handlers.SegmentMapHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ElasticSegmentServiceImpl implements ElasticSegmentService {

    @Autowired
    private ElasticClientService elasticClientService;

    @Override
    public Map<String, List<SegmentMessage>> segmentMap() {
        SegmentMapHandler segmentMapHandler = new SegmentMapHandler(elasticClientService);
        return segmentMapHandler.segmentMap();
    }

    @Override
    public List<SegmentMessage> listSegmentByPrefix(String indexPatternPrefix, String indexXing) throws IOException {
        String api = "/_cat/segments/" + indexXing + "?bytes=m&format=json";
        log.info("获取某种前缀的索引segment列表：{}", api);
        String result = this.elasticClientService.executeGetApi(api);
        JSONArray jsonArray = JSON.parseArray(result);
        List<SegmentMessage> list = new ArrayList<>();

        for (Iterator<Object> itera = jsonArray.iterator(); itera.hasNext(); ) {
            JSONObject next = (JSONObject)itera.next();
            String index = next.getString("index");

            if (!index.startsWith(indexPatternPrefix)) {
                continue;
            }

            Integer shard = next.getInteger("shard");
            String prirep = next.getString("prirep");
            String ip = next.getString("ip");
            String segment = next.getString("segment");
            Long generation = Long.valueOf(next.getLongValue("generation"));
            Long docsCount = Long.valueOf(next.getLongValue("docs.count"));
            Long docsDeleted = Long.valueOf(next.getLongValue("docs.deleted"));
            String size = next.getString("size");

            Long sizeMemory = Long.valueOf(next.getLongValue("size.memory"));

            boolean committed = next.getBooleanValue("committed");
            boolean searchable = next.getBooleanValue("searchable");
            String version = next.getString("version");
            boolean compound = next.getBooleanValue("compound");

            SegmentMessage ms = new SegmentMessage(index, shard, prirep, ip, segment, generation, docsCount, docsDeleted, size, sizeMemory, committed, searchable, version, compound);

            list.add(ms);
        }

        return list;
    }
}
