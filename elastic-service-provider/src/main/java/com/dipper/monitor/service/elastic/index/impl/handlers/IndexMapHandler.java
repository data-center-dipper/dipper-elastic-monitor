package com.dipper.monitor.service.elastic.index.impl.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class IndexMapHandler extends AbstractIndexHandler {

    public IndexMapHandler(ElasticClientService elasticClientService) {
        super(elasticClientService);
    }



    public Map<String, IndexEntity> listIndexMap(boolean setting) throws IOException {
        String response = elasticClientService.executeGetApi(ElasticRestApi.INDEX_LIST.getApiPath());
        JSONArray jsonArray = JSON.parseArray(response);
        Map<String, IndexEntity> map = new HashMap<>();
        jsonArray.forEach(jsonObject -> {
            JSONObject obj = (JSONObject) jsonObject;
            try {
                IndexEntity indexEntity = convertToIndexEntity(obj, setting, false);
                map.put(obj.getString("index"), indexEntity);
            } catch (IOException e) {
                log.error("处理索引数据时发生错误", e);
            }
        });
        return map;
    }
}
