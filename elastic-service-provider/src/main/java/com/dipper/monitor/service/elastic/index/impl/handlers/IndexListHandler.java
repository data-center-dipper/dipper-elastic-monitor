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
import java.util.List;

@Slf4j
public class IndexListHandler extends AbstractIndexHandler {

    public IndexListHandler(ElasticClientService elasticClientService) {
        super(elasticClientService);
    }

    public List<IndexEntity> listIndexList(boolean setting, boolean alians, String status) throws IOException {
        String response = elasticClientService.executeGetApi(ElasticRestApi.INDEX_LIST.getApiPath());
        JSONArray jsonArray = JSON.parseArray(response);
        List<IndexEntity> list = new ArrayList<>();
        for (Object object : jsonArray) {
            JSONObject obj = (JSONObject) object;
            String statusInDb = obj.getString("status");
            if (StringUtils.isNotBlank(status) && !status.equals(statusInDb)) {
                continue;
            }
            try {
                IndexEntity indexEntity = convertToIndexEntity(obj, setting, alians);
                list.add(indexEntity);
            } catch (IOException e) {
                log.error("处理索引数据时发生错误", e);
            }
        }
        return list;
    }
}
