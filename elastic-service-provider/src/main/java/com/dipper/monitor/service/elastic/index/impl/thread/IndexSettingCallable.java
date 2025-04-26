package com.dipper.monitor.service.elastic.index.impl.thread;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.utils.elastic.IndexUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexSettingCallable implements Callable<Map<String, IndexEntity>> {
    private static final Logger log = LoggerFactory.getLogger(IndexSettingCallable.class);

    private List<JSONObject> childList;
    private boolean setting;
    private ElasticClientService elasticClientService;
    private String threadName = null;

    public IndexSettingCallable(List<JSONObject> childList, boolean setting,
                                ElasticClientService elasticClientService) {
        this.childList = childList;
        this.setting = setting;
        this.elasticClientService = elasticClientService;
        this.threadName = Thread.currentThread().getName();
    }

    public Map<String, IndexEntity> call() {
        log.info("解析setting信息：threadName:{} childList：{}",  this.threadName, Integer.valueOf(this.childList.size()));
        Map<String, IndexEntity> allResult = new HashMap<>();

        for (JSONObject obj : this.childList) {
            String health = obj.getString("health");
            String status = obj.getString("status");
            String index = obj.getString("index");
            String uuid = obj.getString("uuid");
            Integer pri = obj.getInteger("pri");
            Integer rep = obj.getInteger("rep");
            Long docsCount = Long.valueOf(obj.getLongValue("docs.count"));
            long docsDeleted = obj.getLongValue("docs.deleted");
            String storeSize = obj.getString("store.size");
            String priStoreSize = obj.getString("pri.store.size");

            IndexEntity indexEntity = new IndexEntity();
            indexEntity.setHealth(health)
                    .setStatus(status)
                    .setIndex(index)
                    .setUuid(uuid)
                    .setPri(pri)
                    .setRep(rep)
                    .setDocsCount(docsCount)
                    .setDocsDeleted(Long.valueOf(docsDeleted))
                    .setStoreSize(storeSize)
                    .setPriStoreSize(priStoreSize);

            if (this.setting && StringUtils.isNotBlank(index) && !IndexUtils.isIndexNameContainSpecialChar(index)) {
                String api = index + "/_settings";
                String settings = null;
                try {
                    settings = this.elasticClientService.executeGetApi(api);
                    indexEntity.setSettings(settings);
                } catch (IOException e) {
                    log.error("查看索引settig异常",e);
                }

            }

            allResult.put(index, indexEntity);
        }

        log.info("解析setting信息完毕 threadName:{} allResult：{}", this.threadName, Integer.valueOf(allResult.size()));
        return allResult;
    }
}