package com.dipper.monitor.service.elastic.index.impl.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.index.IndexEntity;

import com.dipper.monitor.entity.elastic.index.IndexListView;
import com.dipper.monitor.entity.elastic.index.IndexPageReq;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.alians.ElasticAliasService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.utils.Tuple2;
import com.dipper.monitor.utils.elastic.FeatureIndexUtils;
import com.dipper.monitor.utils.elastic.IndexPatternsUtils;
import com.dipper.monitor.utils.elastic.IndexUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class IndexSearchHandler extends AbstractIndexHandler {


    private ElasticAliasService elasticAliasService;
    private ElasticStoreTemplateService elasticStoreTemplateService;

    public IndexSearchHandler(ElasticClientService elasticClientService) {
        super(elasticClientService);

        elasticAliasService = SpringUtil.getBean(ElasticAliasService.class);
        elasticStoreTemplateService = SpringUtil.getBean(ElasticStoreTemplateService.class);
    }



    public Tuple2<List<IndexListView>, Long> indexPageList(IndexPageReq indexPageReq) throws IOException {

        String templateName = indexPageReq.getTemplateName();
        String aliasName = indexPageReq.getAliasName();
        String indexState = indexPageReq.getStatus();
        Boolean featureIndex = indexPageReq.getFeatureIndex();
        if(featureIndex == null){
            featureIndex = false;
        }

        // 1. 获取 indexPatterns（如果提供了 templateName）
        String indexPatterns = "";
        if (StringUtils.isNotBlank(templateName)) {
            EsUnconvertedTemplate template = elasticStoreTemplateService.getOneUnconvertedTemplateByEnName(templateName);
            if (template != null) {
                indexPatterns = IndexPatternsUtils.getIndexPrefixNoDate(template.getIndexPatterns());
            }
        }

        List<IndexEntity> allIndexes = new ArrayList<>();
        if(StringUtils.isBlank(indexPatterns) && StringUtils.isBlank(aliasName)){
            allIndexes = elasticRealIndexService.listIndexList(false, false, indexState);
        }else {
            if(StringUtils.isNotBlank(indexPatterns)){
                List<IndexEntity> allIndexes1 = elasticRealIndexService.listIndexNameByIndexPatterns(indexPatterns, false, false, indexState);
                allIndexes.addAll(allIndexes1);
            }
            if(StringUtils.isNotBlank(aliasName)){
                // 2. 获取所有索引列表，并根据 indexState 初步过滤
                List<IndexEntity> allIndexes2 = elasticRealIndexService.listIndexByAliasName(aliasName,false, false, indexState);
                allIndexes.addAll(allIndexes2);
            }
        }

        if (allIndexes == null || allIndexes.isEmpty()) {
            return new Tuple2<>(Collections.emptyList(), 0L);
        }


        List<IndexEntity> filteredList =  getFeatureIndex(allIndexes,featureIndex);
        if(filteredList == null || filteredList.isEmpty()){
            return new Tuple2<>(Collections.emptyList(), 0L);
        }

        // 7. 设置索引属性（是否可写、别名、配置信息等）
        filteredList = setIndexCanWrite(filteredList);
        filteredList = setIndexAlians(filteredList);
        filteredList = setIndexSetting(filteredList);

        // 8. 转换为返回视图对象
        List<IndexListView> viewList = filteredList.stream()
                .map(this::convertToView)
                .toList();

        // 9. 分页处理
        int total = viewList.size();

        int pageNum = Integer.parseInt(StringUtils.defaultIfBlank(indexPageReq.getPageNum(), "1"));
        int pageSize = Integer.parseInt(StringUtils.defaultIfBlank(indexPageReq.getPageSize(), "10"));

        int fromIndex = Math.min((pageNum - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<IndexListView> pagedList = viewList.subList(fromIndex, toIndex);

        return new Tuple2<>(pagedList, (long) total);
    }

    private List<IndexEntity> getFeatureIndex(List<IndexEntity> allIndexes,boolean featureIndex) {
        if(!featureIndex){
            return allIndexes;
        }
        List<IndexEntity> filteredList = new ArrayList<>();
        for (IndexEntity item: allIndexes) {
            String index = item.getIndex();
            if(FeatureIndexUtils .isFeatureIndex(index)){
                filteredList.add(item);
            }
        }
        return filteredList;
    }



    private IndexListView convertToView(IndexEntity entity) {
        IndexListView view = new IndexListView();
        view.setIndex(entity.getIndex());
        view.setStatus(entity.getStatus());
        view.setAlias(entity.getAliansList());
        view.setIndexCanWrite(entity.getIndexCanWrite());
        view.setAliasCanWrite(entity.getAlinasCanWrite());
        view.setSettings(entity.getSettings());
        // 其他字段按需添加
        return view;
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
            int writeCount = this.elasticAliasService.countAliasWrite(aliasResult,"");
            x.setAlinasCanWrite(Integer.valueOf(writeCount));
        }
        return indexNames;
    }


}
