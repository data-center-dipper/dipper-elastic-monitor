package com.dipper.monitor.service.elastic.index.impl.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.alians.IndexAlias;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.entity.elastic.index.IndexFilterReq;
import com.dipper.monitor.service.elastic.alians.ElasticAliasService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.utils.elastic.IndexUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class IndexSearchHandler extends AbstractIndexHandler {


    private ElasticAliasService elasticAliasService;

    public IndexSearchHandler(ElasticClientService elasticClientService) {
        super(elasticClientService);

        elasticAliasService = SpringUtil.getBean(ElasticAliasService.class);
    }



    public List<IndexEntity> searchIndex(IndexFilterReq indexFilterReq) throws IOException {


        Boolean aliansException = indexFilterReq.getAliansException();

        if (aliansException != null && aliansException.booleanValue()) {
            return getAliansException();
        }

        String indexType = indexFilterReq.getIndexType();
        String indexState = indexFilterReq.getIndexState();
        String healthState = indexFilterReq.getHealthState();
        Boolean feature = indexFilterReq.getFeature();
        String alians = indexFilterReq.getIndexAlians();
        Boolean indexFreeze = indexFilterReq.getFreeze();

        List<IndexEntity> indexNames = elasticRealIndexService.listIndexList(false, false, null);

        // todo: 未完待续

        return indexNames;

    }

    private List<IndexEntity> getAliansException() throws IOException {
        List<String> list = this.elasticAliasService.listExceptionAlias();

        if (list.size() < 1) {
            return Collections.emptyList();
        }

        Map<String, List<IndexAlias>> group = this.elasticAliasService.getAliasIndexMap();
        List<IndexAlias> indexAnlansList = new ArrayList<>();
        for (String index : list) {
            indexAnlansList.addAll(group.get(index));
        }

        Map<String, IndexEntity> indexNamesMap = elasticRealIndexService.listIndexMap(false);
        List<IndexEntity> indexNames = new ArrayList<>();
        for (IndexAlias index : indexAnlansList) {
            indexNames.add(indexNamesMap.get(index.getIndex()));
        }
        //    indexNames = BusinessRelationUtils.indexClassificationByPattern(indexNames, patterns);
        //
        //    Map<String, IndexSetting> indexSettingMap = this.indexGlobalSettingCache.getGlobalIndexSetting();
        //    indexNames = setIndexFreeze(indexNames, indexSettingMap);
        //    indexNames = this.esAliansService.getindexAlians(indexNames);
        indexNames = setIndexCanWrite(indexNames);
        indexNames = setIndexAlians(indexNames);
        indexNames = setIndexSetting(indexNames);
        return indexNames;
    }

    public List<IndexEntity> setIndexSetting(List<IndexEntity> indexNames) {
        indexNames.stream().forEach(x -> x.setSettings(""));
        return indexNames;
    }

    public List<IndexEntity> setIndexAlians(List<IndexEntity> indexNames) {
        Map<String, JSONObject> aliansMap = null;
        try {
            aliansMap = this.elasticAliasService.getAllAliasJson();
        } catch (Exception e) {
            log.error("获取ES所有别名异常：{}", e.getMessage(), e);
        }
        if (aliansMap == null) {
            return indexNames;
        }
        for (IndexEntity item : indexNames) {
            String index = item.getIndex();
            JSONObject aliansJson = aliansMap.get(index);
            if (aliansJson != null) {
                List<String> aliansList = IndexUtils.getAliansListFromAliansSet(aliansJson);
                item.setAlians(StringUtils.join(aliansList.toArray(), ","));
                item.setAliansList(aliansList);
            }
        }
        return indexNames;
    }

    public List<IndexEntity> setIndexCanWrite(List<IndexEntity> indexNames) throws IOException {
        for (IndexEntity x : indexNames) {
            String indexSetting = x.getSettings();
            JSONObject indexSettingObj = JSON.parseObject(indexSetting);
            String indexReadOnly = (String) JSONPath.eval(indexSettingObj, "$..settings.index.blocks.read_only[0]");
            if ("true".equals(indexReadOnly)) {
                x.setIndexCanWrite(Boolean.valueOf(false));
                continue;
            }
            String canWrite = (String) JSONPath.eval(indexSettingObj, "$..settings.index.blocks.write[0]");
            if ("false".equals(canWrite)) {
                x.setIndexCanWrite(Boolean.valueOf(true));
            } else if ("true".equals(canWrite)) {
                x.setIndexCanWrite(Boolean.valueOf(false));
            } else {
                x.setIndexCanWrite(Boolean.valueOf(true));
            }
            String aliasResult = x.getAlians();
            int writeCount = this.elasticAliasService.countAliasWrite(aliasResult);
            x.setAlinasCanWrite(Integer.valueOf(writeCount));
        }
        return indexNames;
    }


}
