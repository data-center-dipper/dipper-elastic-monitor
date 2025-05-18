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
        String startTime = indexPageReq.getStartTime(); // 格式如 "2025.05.01"
        String endTime = indexPageReq.getEndTime();     // 格式如 "2025.05.18"

        // 1. 获取 indexPatterns（如果提供了 templateName）
        String indexPatterns = "";
        if (StringUtils.isNotBlank(templateName)) {
            EsUnconvertedTemplate template = elasticStoreTemplateService.getOneUnconvertedTemplateByEnName(templateName);
            if (template != null) {
                indexPatterns = template.getIndexPatterns();
            }
        }

        // 2. 获取所有索引列表，并根据 indexState 初步过滤
        List<IndexEntity> allIndexes = elasticRealIndexService.listIndexList(false, false, indexState);

        if (allIndexes == null || allIndexes.isEmpty()) {
            return new Tuple2<>(Collections.emptyList(), 0L);
        }

        List<IndexEntity> filteredList = new ArrayList<>();

        for (IndexEntity entity : allIndexes) {
            boolean match = true;

            // 3. 按 indexPatterns 匹配索引名（支持通配符 * 和 ?）
            if (StringUtils.isNotBlank(indexPatterns)) {
                if (!IndexUtils.isMatchPattern(entity.getIndex(), indexPatterns)) {
                    match = false;
                }
            }

            // 4. 按 aliasName 匹配索引的别名
            if (StringUtils.isNotBlank(aliasName)) {
                if (entity.getAliansList() == null || !entity.getAliansList().contains(aliasName)) {
                    match = false;
                }
            }

            // 5. 按 featureIndex 特性匹配（示例逻辑，可根据实际业务调整）
            if (Boolean.TRUE.equals(featureIndex)) {
                if (!entity.getIndex().startsWith("feature_")) { // 示例规则
                    match = false;
                }
            }

            // 6. 按时间范围匹配索引名中的日期部分（需 IndexUtils 支持）
            if (match && (StringUtils.isNotBlank(startTime) || StringUtils.isNotBlank(endTime))) {
                if (!IndexUtils.isInDateRange(entity.getIndex(), startTime, endTime)) {
                    match = false;
                }
            }

            if (match) {
                filteredList.add(entity);
            }
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
