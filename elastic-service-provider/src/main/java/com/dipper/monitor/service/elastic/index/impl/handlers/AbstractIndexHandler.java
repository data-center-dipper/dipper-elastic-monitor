package com.dipper.monitor.service.elastic.index.impl.handlers;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticIndexService;
import com.dipper.monitor.utils.elastic.IndexUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

@Slf4j
public abstract class AbstractIndexHandler {


    protected  ElasticClientService elasticClientService;
    protected ElasticIndexService elasticIndexService;

    public AbstractIndexHandler(ElasticClientService elasticClientService) {
        this.elasticClientService = elasticClientService;
        elasticIndexService = SpringUtil.getBean(ElasticIndexService.class);
    }

    protected IndexEntity convertToIndexEntity(JSONObject obj, boolean setting, boolean alians) throws IOException {

        String health = obj.getString("health");
        String status = obj.getString("status");
        String index = obj.getString("index");
        if (health == null || StringUtils.isBlank(health) || "null".equals(health)) {
            health = "empty";
        }
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setHealth(health)
                .setStatus(status)
                .setIndex(index)
                .setUuid(obj.getString("uuid"))
                .setPri(obj.getInteger("pri"))
                .setRep(obj.getInteger("rep"))
                .setDocsCount(Long.valueOf(obj.getLongValue("docs.count")))
                .setDocsDeleted(Long.valueOf(obj.getLongValue("docs.deleted")))
                .setStoreSize(obj.getString("store.size"))
                .setPriStoreSize(obj.getString("pri.store.size"));

        if (setting && StringUtils.isNotBlank(index) && !IndexUtils.isIndexNameContainSpecialChar(index)) {
            String api = index + "/_settings";
            try {
                String settings = elasticClientService.executeGetApi(api);
                indexEntity.setSettings(settings);
            } catch (IOException e) {
                log.error("获取索引setting失败", e);
            }
        }
        if (alians && StringUtils.isNotBlank(index) && !IndexUtils.isIndexNameContainSpecialChar(index)) {
            String api = index + "/_alias";
            try {
                String aliases = elasticClientService.executeGetApi(api);
                indexEntity.setAlians(aliases);
            } catch (IOException e) {
                log.error("获取索引：{} 别名信息异常:{}", index, e.getMessage());
            }
        }
        return indexEntity;
    }

}
