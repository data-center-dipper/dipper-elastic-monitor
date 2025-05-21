package com.dipper.monitor.service.elastic.index.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.alians.IndexAliasRelation;
import com.dipper.monitor.entity.elastic.index.*;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.enums.elastic.IndexOperatorType;
import com.dipper.monitor.service.elastic.alians.ElasticAliasService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.index.IndexOneOperatorService;
import com.dipper.monitor.service.elastic.index.IndexStatusService;
import com.dipper.monitor.service.elastic.index.impl.handlers.*;
import com.dipper.monitor.service.elastic.index.impl.thread.IndexSettingCallable;
import com.dipper.monitor.service.elastic.life.ElasticRealLifecyclePoliciesService;
import com.dipper.monitor.service.elastic.segment.ElasticSegmentService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.utils.CommonThreadFactory;
import com.dipper.monitor.utils.ListUtils;
import com.dipper.monitor.utils.ResultUtils;
import com.dipper.monitor.utils.Tuple2;
import com.dipper.monitor.utils.elastic.BytesUtil;
import com.dipper.monitor.utils.elastic.EsDateUtils;
import com.dipper.monitor.utils.elastic.IndexPatternsUtils;
import com.dipper.monitor.utils.elastic.IndexUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.nio.entity.NStringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ElasticRealIndexServiceImpl implements ElasticRealIndexService {


    @Autowired
    private ElasticClientService elasticClientService;

    @Autowired
    private ElasticShardService elasticShardService;

    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;

    @Autowired
    private ElasticAliasService elasticAliasService;

    @Autowired
    private ElasticSegmentService elasticSegmentService;

    @Autowired
    private ElasticRealLifecyclePoliciesService elasticRealLifecyclePoliciesService;
    @Autowired
    private IndexStatusService indexStatusService;
    @Autowired
    private IndexOneOperatorService indexOneOperatorService;


    private Cache<String, Object> cache;

    private static volatile BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(120);
    private static volatile ThreadPoolExecutor delegate = new ThreadPoolExecutor(10,
            30, 1L, TimeUnit.HOURS, queue,
            (ThreadFactory) new CommonThreadFactory("elasticIndexService"),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    ListeningExecutorService executorService = MoreExecutors.listeningDecorator(delegate);

    @PostConstruct
    public void init() {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(7L)
                .expireAfterWrite(300L, TimeUnit.SECONDS)
                .expireAfterAccess(300L, TimeUnit.SECONDS)
                .concurrencyLevel(7).build();
    }


    public long getIndexDocCount(String index) throws IOException {
        Long indexDocCount = Long.valueOf(0L);
        String flowIndexSettings = this.elasticClientService.executeGetApi(index.concat("/_count"));
        JSONObject json = JSON.parseObject(flowIndexSettings);
        if (json.containsKey("_count")) {
            indexDocCount = json.getLong("_count");
        }
        return indexDocCount.longValue();
    }


    public Map<String, IndexEntity> listIndexMap(boolean getSetting) throws IOException {
        IndexMapHandler indexMapHandler = new IndexMapHandler(elasticClientService);
        Map<String, IndexEntity> map = indexMapHandler.listIndexMap(getSetting);
        return map;
    }

    public List<IndexEntity> listIndexList(boolean setting, boolean alias, String status) throws IOException {
        IndexListHandler indexListHandler = new IndexListHandler(elasticClientService);
        List<IndexEntity> indexEntities = indexListHandler.listIndexList(setting, alias, status);
        return indexEntities;
    }

    /**
     * 根据前缀获取索引列表
     *
     * @param indexPrefix 索引前缀 ailpha-baas-flow-
     * @param indexXing   索引模糊匹配 ailpha-baas-flow-*
     * @return
     * @throws IOException
     */
    @Override
    public List<IndexEntity> listIndexNameByPrefix(String indexPrefix, String indexXing) throws IOException {
        String api = "/_cat/indices/" + indexXing + "?format=json";
        log.info("获取某种类型的索引：{}", api);
        String res1 = this.elasticClientService.executeGetApi(api);
        JSONArray jsonArray = JSON.parseArray(res1);

        List<JSONObject> filteredList = jsonArray.toJavaList(JSONObject.class).stream()
                .filter(jsonObject -> ((JSONObject) jsonObject).getString("index").startsWith(indexPrefix))
                .collect(Collectors.toList());

        log.info("前缀为 {} 的索引个数：{}", indexPrefix, filteredList.size());

        List<List<JSONObject>> indexList = ListUtils.splitListBySize(filteredList, 200);
        if (CollectionUtils.isEmpty(indexList)) {
            return Collections.emptyList();
        }

        List<Future<Map<String, IndexEntity>>> futureList = new ArrayList<>();
        for (List<JSONObject> childList : indexList) {
            log.info("多线程提交获取索引的设置：{}", childList.size());
            ListenableFuture<Map<String, IndexEntity>> listenableFuture = this.executorService.submit(
                    new IndexSettingCallable(childList, true, this.elasticClientService));
            futureList.add(listenableFuture);
        }

        List<IndexEntity> allResult = new ArrayList<>();
        for (Future<Map<String, IndexEntity>> future : futureList) {
            try {
                Map<String, IndexEntity> result = future.get(40L, TimeUnit.SECONDS);
                if (result != null) {
                    allResult.addAll(result.values());
                }
            } catch (Exception e) {
                log.error("从多线程中获取索引执行结果异常：{} feature:{} isCancelled:{}",
                        e.getMessage(), future.isDone(), future.isCancelled(), e);
            }
        }

        log.info("获取多线程执行的总结果：{}", allResult.size());
        return allResult;
    }


    public Tuple2<List<IndexListView>, Long> indexPageList(IndexPageReq indexPageReq) throws IOException {
        IndexSearchHandler indexListHandler = new IndexSearchHandler(elasticClientService);
        Tuple2<List<IndexListView>, Long> indexEntities = indexListHandler.indexPageList(indexPageReq);
        return indexEntities;

    }

    @Override
    public JSONObject indexTemplate(String indexName) {
        ElasticRealTemplateService elasticRealTemplateService = SpringUtil.getBean(ElasticRealTemplateService.class);
        return elasticRealTemplateService.getTemplateByIndexName(indexName);
    }

    @Override
    public List<IndexEntity> listIndexByAliasName(String aliasName, boolean setting, boolean alias, String status) throws IOException {
        IndexListByAliasHandler indexListHandler = new IndexListByAliasHandler(elasticClientService);
        List<IndexEntity> indexEntities = indexListHandler.listIndexByAliasName(aliasName, setting, alias, status);
        return indexEntities;
    }

    @Override
    public List<IndexEntity> listIndexNameByIndexPatterns(String indexPatterns, boolean b, boolean b1, String indexState) throws IOException {
        String indexPrefixNoDate = IndexPatternsUtils.getIndexPrefixNoDate(indexPatterns);
        String indexXing = indexPrefixNoDate + "*";
        String api = "/_cat/indices/" + indexXing + "?format=json";
        log.info("获取某种类型的索引：{}", api);
        String res1 = this.elasticClientService.executeGetApi(api);
        JSONArray jsonArray = JSON.parseArray(res1);

        List<JSONObject> filteredList = jsonArray.toJavaList(JSONObject.class).stream()
                .filter(jsonObject -> ((JSONObject) jsonObject).getString("index").startsWith(indexPrefixNoDate))
                .collect(Collectors.toList());

        log.info("前缀为 {} 的索引个数：{}", indexPrefixNoDate, filteredList.size());

        List<IndexEntity> result = new ArrayList<>();
        for (JSONObject childList : filteredList) {
            IndexEntity indexEntity = convertToIndexEntity(childList);
            result.add(indexEntity);
        }
        return result;
    }

    private IndexEntity convertToIndexEntity(JSONObject obj) {
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
        return indexEntity;
    }

    private void setExtraLabel(List<IndexEntity> indexEntities) {
        List<EsLifeCycleManagement> lifeCycleList = this.elasticRealLifecyclePoliciesService.getLifeCycleList();
        Map<String, EsLifeCycleManagement> lifecycleMap = lifeCycleList.stream()
                .collect(Collectors.toMap(EsLifeCycleManagement::getIndex,
                        esLifeCycleManagement -> esLifeCycleManagement));

        for (IndexEntity indexEntity : indexEntities) {
            if (indexEntity.getAliansList() != null) {
                JSONObject aliasJson = indexEntity.getAliansJson();
                JSONObject aliases = aliasJson.getJSONObject("aliases");

                if (!indexEntity.getAliansList().isEmpty()) {
                    Boolean writeIndex = aliases.getJSONObject(indexEntity.getAliansList().get(0)).getBoolean("is_write_index");
                    indexEntity.setIndexCanWrite(writeIndex);
                }
            }

            if (lifecycleMap.containsKey(indexEntity.getIndex())) {
                indexEntity.setLifecycleStatus(Boolean.valueOf(false));
            } else {
                indexEntity.setLifecycleStatus(Boolean.valueOf(true));
            }

            if (StringUtils.isNotBlank(indexEntity.getStoreSize())) {
                indexEntity.setStoreSizeWithUnit(BytesUtil.transKbWithUnit(Float.parseFloat(indexEntity.getStoreSize())));
            } else {
                indexEntity.setStoreSizeWithUnit("");
            }

            if (indexEntity.getPri() == null || indexEntity.getRep() == null) {
                indexEntity.setPri(null);
                continue;
            }
            indexEntity.setPri(Integer.valueOf(indexEntity.getPri().intValue() * (1 + indexEntity.getRep().intValue())));
        }
    }


    private List<IndexEntity> filterByCanWrite(List<IndexEntity> indexNames) {
        List<IndexEntity> list = new ArrayList<>(indexNames.size());
        indexNames.stream().forEach(x -> {
            boolean alians = x.getIndexCanWrite().booleanValue();
            if (alians) {
                list.add(x);
            }
        });
        return list;
    }

    private List<IndexEntity> filterByAliansList(List<IndexEntity> indexNames) {
        Map<String, List<IndexAliasRelation>> alinsEx = new HashMap<>();
        try {
            alinsEx = this.elasticAliasService.listExceptionAlias();
        } catch (IOException e) {
            log.error("获取异常的别名出错：{}", e.getMessage(), e);
        }

        List<IndexEntity> list = new ArrayList<>();

        for (IndexEntity x : indexNames) {
            String alias = x.getAlians();

            // 如果别名为空，跳过
            if (StringUtils.isBlank(alias)) {
                continue;
            }

            // 检查是否是异常别名（即是否存在于 alinsEx 的 keySet 中）
            if (alinsEx.containsKey(alias)) {
                list.add(x);
            }
        }

        return list;
    }



    @Deprecated
    private List<IndexEntity> filterByFrozen(List<IndexEntity> indexNames) {
        List<IndexEntity> list = new ArrayList<>();
        indexNames.stream().forEach(x -> {
            try {
                String settings = x.getSettings();
                if (settings != null) {
                    if (settings.contains("\"frozen\":\"true\"")) {
                        x.setStatus("freeze");
                        list.add(x);
                    }
                }
            } catch (Exception e) {
                log.error("获取索引是否冻结失败：{} e:{}", x.getIndex(), e.getMessage(), e);
            }
        });
        return list;
    }

    private JSONArray getIndexSetting(JSONArray indexNames) {
        indexNames.stream().forEach(jsonObject -> {
            JSONObject obj = (JSONObject) jsonObject;
            String index = obj.getString("index");
            if (StringUtils.isNotBlank(index) && !IndexUtils.isIndexNameContainSpecialChar(index)) {
                String api = index + "/_settings";
                String settings = null;
                try {
                    settings = elasticClientService.executeGetApi(api);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                obj.put("_settings", settings);
            }
        });
        return indexNames;
    }

    @Deprecated
    public List<IndexEntity> getIndexListSetting(List<IndexEntity> indexNames) {
        for (IndexEntity item : indexNames) {
            String index = item.getIndex();
            if (StringUtils.isNotBlank(index) && !IndexUtils.isIndexNameContainSpecialChar(index)) {
                String api = index + "/_settings";
                String settings = null;
                try {
                    settings = elasticClientService.executeGetApi(api);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                item.setSettings(settings);
            }
        }
        return indexNames;
    }


    public List<IndexEntity> listIndexByPrefix(boolean setting, String indexPrefix, String indexXing) throws IOException {
        String api = "/_cat/indices/" + indexXing + "?format=json";
        log.info("获取某种类型的索引：{}", api);
        String res1 = this.elasticClientService.executeGetApi(api);
        JSONArray jsonArray = JSON.parseArray(res1);

        List<JSONObject> filteredList = jsonArray.toJavaList(JSONObject.class).stream()
                .filter(jsonObject -> ((JSONObject) jsonObject).getString("index").startsWith(indexPrefix))
                .collect(Collectors.toList());

        log.info("前缀为 {} 的索引个数：{}", indexPrefix, filteredList.size());

        List<List<JSONObject>> indexList = ListUtils.splitListBySize(filteredList, 200);
        if (CollectionUtils.isEmpty(indexList)) {
            return Collections.emptyList();
        }

        List<Future<Map<String, IndexEntity>>> futureList = new ArrayList<>();
        for (List<JSONObject> childList : indexList) {
            log.info("多线程提交获取索引的设置：{}", childList.size());
            ListenableFuture<Map<String, IndexEntity>> listenableFuture = this.executorService.submit(
                    new IndexSettingCallable(childList, setting, this.elasticClientService));
            futureList.add(listenableFuture);
        }

        List<IndexEntity> allResult = new ArrayList<>();
        for (Future<Map<String, IndexEntity>> future : futureList) {
            try {
                Map<String, IndexEntity> result = future.get(40L, TimeUnit.SECONDS);
                if (result != null) {
                    allResult.addAll(result.values());
                }
            } catch (Exception e) {
                log.error("从多线程中获取broker jmx 执行结果异常：{} feature:{} isCancelled:{}",
                        e.getMessage(), future.isDone(), future.isCancelled(), e);
            }
        }

        log.info("获取多线程执行的总结果：{}", allResult.size());
        return allResult;
    }


    /**
     * 根据索引模式获取索引列表
     *
     * @param indexPatterns 索引模式，如 lcc-log-YYYYMMDD
     * @return 索引列表
     * @throws IOException 异常
     */
    @Override
    public List<String> listIndexNameByPrefix(String indexPatterns) throws IOException {
        // 从索引模式中提取前缀
        String indexPrefix = null;
        String indexXing = null;

        if (indexPatterns.contains("yyyy")) {
            // 如果包含日期格式，提取日期前的部分作为前缀
            indexPrefix = indexPatterns.substring(0, indexPatterns.indexOf("yyyy"));
            indexXing = indexPrefix + "*";
        } else {
            // 如果包含通配符，提取通配符前的部分作为前缀
            int position = indexPatterns.indexOf("*");
            if (position < 1) {
                position = indexPatterns.length();
            }
            indexPrefix = indexPatterns.substring(0, position);
            indexXing = indexPatterns;
        }

        // 调用已有方法获取索引实体列表
        List<IndexEntity> indexEntities = listIndexNameByPrefix(indexPrefix, indexXing);

        // 转换为索引名称列表
        List<String> indices = indexEntities.stream()
                .map(IndexEntity::getIndex)
                .collect(Collectors.toList());

        return indices;
    }

    /**
     * 获取某种类型索引的前缀
     *
     * @param indexPatterns lcc-log-YYYYMMDD
     * @param indexPrefix   lcc-log-
     * @param indexXing     lcc-log-*
     * @return
     * @throws IOException
     */
    public List<IndexEntity> listIndexNameByPrefix(String indexPatterns, String indexPrefix, String indexXing) throws IOException {
        List<IndexEntity> strings = listIndexNameByPrefix(indexPrefix, indexXing);
        return strings;
    }


    @Override
    public Map<String, IndexEntity> listIndexPatternMapThread(boolean setting, String indexPatternPrefix, String indexXing) throws IOException {
        IndexListPatternThreadHandler indexMapHandler = new IndexListPatternThreadHandler(elasticClientService);
        Map<String, IndexEntity> map = indexMapHandler.listIndexPatternMapThread(setting, indexPatternPrefix, indexXing);
        return map;
    }

    @Override
    public String createIndex(String indexName) {
        if (StringUtils.isBlank(indexName)) {
            log.warn("索引名称为空，无法创建索引");
            throw new RuntimeException("索引名称为空");
        }

        try {
            // 调用ES API创建索引
            String result = elasticClientService.executePutApi(indexName, null);


            log.info("创建索引 {} 成功", indexName);
            return result;
        } catch (Exception e) {
            log.error("创建索引异常：index:{}  ex:{}", indexName, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public String createIndex(String indexName, JSONObject templateJson) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(indexName)) {
            log.warn("索引名称为空，无法创建索引");
            throw new RuntimeException("索引名称为空");
        }

        if (templateJson == null || templateJson.isEmpty()) {
            log.warn("模板JSON为空，无法创建索引");
            throw new RuntimeException("模板JSON为空");
        }

        try {

            String requestBodyStr = templateJson.toJSONString();
            NStringEntity entity = new NStringEntity(requestBodyStr);
            // 调用ES API创建索引
            String result = elasticClientService.executePutApi(indexName, entity);
            entity.close();

            log.info("创建索引 {} 成功", indexName);
            return result;
        } catch (Exception e) {
            log.error("创建索引异常：index:{} templateJson:{} ex:{}", indexName, templateJson, e.getMessage(), e);
            throw e;
        }
    }


    public Map<String, IndexSetting> getGlobalIndexSettingFromEs() throws IOException {
        String result = this.elasticClientService.executeGetApi("/*/_settings");
        if (StringUtils.isBlank(result)) {
            return Collections.emptyMap();
        }

        JSONObject settingsJson = JSON.parseObject(result);
        Map<String, IndexSetting> indexMap = new HashMap<>(settingsJson.size());

        settingsJson.forEach((index, jsonObject) -> {
            JSONObject jsonObjectSetting = (JSONObject) jsonObject;
            IndexSetting indexSetting = parseIndexSetting(index, jsonObjectSetting);
            indexMap.put(index, indexSetting);
        });

        return indexMap;
    }

    private IndexSetting parseIndexSetting(String index, JSONObject jsonObjectSetting) {
        IndexSetting indexSetting = new IndexSetting();
        indexSetting.setSettingData(jsonObjectSetting);
        indexSetting.setIndex(index);

        Boolean frozen = jsonObjectSetting.getJSONObject("settings")
                .getBoolean("index.frozen");
        indexSetting.setFreeze(frozen == null ? false : frozen);

        Boolean blocksWrite = jsonObjectSetting.getJSONObject("settings")
                .getBoolean("index.blocks.write");
        indexSetting.setBlocksWrite(blocksWrite == null ? false : blocksWrite);

        return indexSetting;
    }

    public IndexSetting initOneIndexSetting(String index) {
        String result = null;
        try {
            result = this.elasticClientService.executeGetApi("/" + index + "/_settings");
        } catch (IOException e) {
            log.error("获取 {} 索引的setting异常：{}", index, e.getMessage(), e);
            return null;
        }
        if (StringUtils.isBlank(result)) {
            return null;
        }

        JSONObject setting = JSON.parseObject(result);
        JSONObject jsonObjectSetting = setting.getJSONObject(index);
        return parseIndexSetting(index, jsonObjectSetting);
    }



    private Boolean isIndexCondition(String index, String prefix, String format, boolean checkFuture) {
        Integer nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt(format));
        String[] parts = index.split("-");
        if (parts.length < 2) {
            log.error("索引 {} 格式不正确", index);
            return false;
        }
        String datePart = parts[parts.length - 2];
        if (!isAllNumber(datePart)) {
            log.error("索引：{} 倒数第二个信息不是数字不予操作", index);
            return false;
        }
        int indexDateTimeInt = Integer.parseInt(datePart);
        return checkFuture ? (nowDateTime.intValue() < indexDateTimeInt) : (nowDateTime.intValue() == indexDateTimeInt);
    }

    private Boolean isIndexFeature(String index, String prefix, String format) {
        return isIndexCondition(index, prefix, format, true);
    }

    private Boolean isIndexNow(String index, String prefix, String format) {
        return isIndexCondition(index, prefix, format, false);
    }

    private static final Pattern pattern = Pattern.compile("[0-9]*");

    public boolean isAllNumber(String dest) {
        return pattern.matcher(dest).matches();
    }
}