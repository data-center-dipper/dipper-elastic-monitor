package com.dipper.monitor.service.elastic.index.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.service.elastic.alians.ElasticAliansService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticIndexService;
import com.dipper.monitor.service.elastic.life.LifecyclePoliciesService;
import com.dipper.monitor.service.elastic.segment.ElasticSegmentService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import com.dipper.monitor.service.elastic.template.ElasticTemplateService;
import com.dipper.monitor.utils.CommonThreadFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.*;

@Service
@Slf4j
public class ElasticIndexServiceImpl implements ElasticIndexService {


    @Autowired
    private ElasticClientService elasticClientService;

    @Autowired
    private ElasticShardService elasticShardService;

    @Autowired
    private ElasticTemplateService elasticTemplateService;

    @Autowired
    private ElasticAliansService elasticAliansService;

    @Autowired
    private ElasticSegmentService elasticSegmentService;

    @Autowired
    private LifecyclePoliciesService lifecyclePoliciesService;


    private Cache<String, Object> cache;

    private static volatile BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(120);
    private static volatile ThreadPoolExecutor delegate = new ThreadPoolExecutor(10,
            30, 1L, TimeUnit.HOURS, queue,
            (ThreadFactory) new CommonThreadFactory("elasticIndexService"),
            new ThreadPoolExecutor.DiscardOldestPolicy());

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


}

@Deprecated
public List<EsTemplateConfigMes> listStatisticalByLift(List<EsTemplateConfigMes> temList) throws IOException {
    long end2 = System.currentTimeMillis();

    Map<String, EsLifeCycleManagement> lifeErrors = this.indexLifecyclePoliciesService.getLifeCycleBadMap();

    long end3 = System.currentTimeMillis();
    log.info("时间 模板 周期问题：{}", Long.valueOf(end3 - end2));

    Map<String, IndexEntity> indexMap = this.esRestClient.listIndexMap(false);

    long end4 = System.currentTimeMillis();
    log.info("时间 模板 索引：{}", Long.valueOf(end4 - end3));

    Map<String, List<Shard>> listShard = this.shardService.listShardMap();

    long end5 = System.currentTimeMillis();
    log.info("时间 模板 shard：{}", Long.valueOf(end5 - end4));

    Map<String, List<SegmentMessage>> segmentMap = this.esSegmentService.segmentMap();

    long end6 = System.currentTimeMillis();
    log.info("时间 模板 segment：{}", Long.valueOf(end6 - end5));

    Map<EsTemplateConfigMes, List<IndexEntity>> groupIndex = groupIndexByTem(temList, indexMap);
    Map<String, IndexSetting> indexSettingMap = this.indexGlobalSettingCache.getGlobalIndexSetting();

    List<EsTemplateConfigMes> result = new ArrayList<>();
    for (Map.Entry<EsTemplateConfigMes, List<IndexEntity>> item : groupIndex.entrySet()) {
        List<IndexEntity> indexs = item.getValue();
        EsTemplateConfigMes key = item.getKey();

        EsTemplateConfigMes et = new EsTemplateConfigMes();
        BeanUtils.copyProperties(key, et);

        et.setTemplateConfigNameValue("");
        et.setCountIndex(Integer.valueOf(indexs.size()));

        int rollingCycleError = 0;
        int openIndex = 0;
        int closeIndex = 0;
        int exceptionIndex = 0;
        int freezeIndex = 0;

        int shardCount = 0;
        int shardUnassigned = 0;

        int segmetCount = 0;
        long segmentSize = 0L;

        for (IndexEntity indexEntity : indexs) {
            String index = indexEntity.getIndex();

            if (lifeErrors.get(index) != null) {
                rollingCycleError++;
            }

            List<Shard> shardList = listShard.get(index);
            if (shardList != null) {
                shardCount += shardList.size();
                for (Shard shard : shardList) {
                    String shardState = shard.getState();
                    if ("UNASSIGNED".equalsIgnoreCase(shardState)) {
                        shardUnassigned++;
                    }
                }
            }

            List<SegmentMessage> segmentList = segmentMap.get(index);
            if (segmentList != null) {
                segmetCount += segmentList.size();
                for (SegmentMessage sm : segmentList) {
                    Long sizeMemory = sm.getSizeMemory();
                    segmentSize += sizeMemory.longValue();
                }
            }

            String status = indexEntity.getStatus();
            if (status.equals("open")) {
                openIndex++;
            }
            if (status.equals("close")) {
                closeIndex++;
            }

            String health = indexEntity.getHealth();
            if ("red".equalsIgnoreCase(health) || "yellow".equalsIgnoreCase(health)) {
                exceptionIndex++;
            }

            IndexSetting indexSetting = indexSettingMap.get(index);
            if (indexSetting == null) {
                indexSetting = this.indexGlobalSettingCache.initOneIndexSetting(index);
            }
            if (indexSetting != null) {
                if (indexSetting.getFreeze().booleanValue()) {
                    freezeIndex++;
                }
            }
        }

        et.setRollingCycleError(Integer.valueOf(rollingCycleError));
        et.setOpenIndex(Integer.valueOf(openIndex));
        et.setCloseIndex(Integer.valueOf(closeIndex));
        et.setExceptionIndex(Integer.valueOf(exceptionIndex));
        et.setFreezeIndex(Integer.valueOf(freezeIndex));

        et.setShardCount(Integer.valueOf(shardCount));
        et.setShardUnassigned(Integer.valueOf(shardUnassigned));

        et.setSegmetCount(Integer.valueOf(segmetCount));
        et.setSegmentSize(Long.valueOf(segmentSize / 1048576L));

        result.add(et);
    }

    long end7 = System.currentTimeMillis();
    log.info("时间 模板 遍历：{}", Long.valueOf(end7 - end6));

    result = this.esTemplateService.sortStatCommon(result);

    return result;
}

private Map<EsTemplateConfigMes, List<IndexEntity>> groupIndexByTem(List<EsTemplateConfigMes> temList, Map<String, IndexEntity> indexMap) {
    Map<EsTemplateConfigMes, List<IndexEntity>> mapGroup = new HashMap<>();

    for (EsTemplateConfigMes esTemplateConfigMes : temList) {
        String name = esTemplateConfigMes.getTemplateConfigName();
        String templateConfigNameValue = esTemplateConfigMes.getTemplateConfigNameValue();
        JSONObject json = JSON.parseObject(templateConfigNameValue);
        JSONArray array = (JSONArray)JSONPath.eval(json, "$.index_patterns");
        if (array == null || array.size() < 1) {
            continue;
        }

        Set<IndexEntity> indexSet = new HashSet<>();
        for (Iterator<Object> itera = array.iterator(); itera.hasNext(); ) {
            String indexPattern = (String)itera.next();

            String indexPrefxi = null;
            if (indexPattern.contains("yyyy")) {
                indexPrefxi = indexPattern.substring(0, indexPattern.indexOf("yyyy"));
            } else {
                indexPrefxi = indexPattern.substring(0, indexPattern.indexOf("*"));
            }
            for (Map.Entry<String, IndexEntity> item : indexMap.entrySet()) {
                String index = item.getKey();

                if (!index.startsWith(indexPrefxi)) {
                    continue;
                }

                indexSet.add(item.getValue());
            }
        }
        mapGroup.put(esTemplateConfigMes, new ArrayList<>(indexSet));
    }
    return mapGroup;
}

public List<IndexEntity> listIndex(IndexFilterReq indexFilterReq) throws IOException {
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

    List<IndexEntity> indexNames = this.esRestClient.listIndexList(false, false, null);
    CommonProps relation = this.commonPropsRepo.queryByKey("templateAndBusinessRelation");
    Map<String, BusinessRelation> patterns = BusinessRelationUtils.getAndCreatePattern(relation);
    indexNames = BusinessRelationUtils.indexClassificationByPattern(indexNames, patterns);

    if (StringUtils.isBlank(indexType)) {
        indexType = "all";
    }
    indexNames = BusinessRelationUtils.filterByGroup(indexNames, indexType);

    if (StringUtils.isNotBlank(healthState)) {
        switch (healthState) {
            case "red":
                indexNames = BusinessRelationUtils.filterByHelath(indexNames, "red");
                break;
            case "green":
                indexNames = BusinessRelationUtils.filterByHelath(indexNames, "green");
                break;
            case "yellow":
                indexNames = BusinessRelationUtils.filterByHelath(indexNames, "yellow");
                break;
        }
    }

    indexNames = BusinessRelationUtils.setIndexFeature(indexNames);

    if (feature != null && feature.booleanValue()) {
        indexNames = BusinessRelationUtils.indexFeature(indexNames);
    }

    Map<String, IndexSetting> indexSettingMap = this.indexGlobalSettingCache.getGlobalIndexSetting();
    indexNames = setIndexFreeze(indexNames, indexSettingMap);

    if (StringUtils.isNotBlank(indexState)) {
        indexNames = BusinessRelationUtils.filterByState(indexNames, indexState);
    }

    if (indexFreeze != null && indexFreeze.booleanValue()) {
        indexNames = BusinessRelationUtils.filterByFreeze(indexNames);
    }

    indexNames = this.esAliansService.getindexAlians(indexNames);

    if (StringUtils.isNotBlank(alians)) {
        indexNames = filterByAlians(indexNames, alians);
    }

    setExtraLabel(indexNames);

    if (indexFilterReq.getLifecycleStatus() != null) {
        indexNames = (List<IndexEntity>)indexNames.stream().filter(indexEntity -> indexFilterReq.getLifecycleStatus().equals(indexEntity.getLifecycleStatus())).collect(Collectors.toList());
    }

    indexNames = setIndexSetting(indexNames);

    String orderBy = indexFilterReq.getOrderBy();
    String order = indexFilterReq.getOrder();

    if (2 < indexNames.size()) {
        indexNames = orderData(indexNames, orderBy, order);
    }

    return indexNames;
}

private void setExtraLabel(List<IndexEntity> indexEntities) {
    List<EsLifeCycleManagement> lifeCycleList = this.indexLifecyclePoliciesService.getLifeCycleList();
    Map<String, EsLifeCycleManagement> lifecycleMap = lifeCycleList.stream().collect(Collectors.toMap(EsLifeCycleManagement::getIndex, esLifeCycleManagement -> esLifeCycleManagement));

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

private List<IndexEntity> getAliansException() throws IOException {
    List<String> list = this.esAliansService.listExceptionAlians();

    if (list.size() < 1) {
        return Collections.emptyList();
    }

    Map<String, List<IndexAlians>> group = this.esAliansService.getAliansIndexMap();
    List<IndexAlians> indexAnlansList = new ArrayList<>();
    for (String index : list) {
        indexAnlansList.addAll(group.get(index));
    }

    Map<String, IndexEntity> indexNamesMap = this.esRestClient.listIndexMap(false);
    List<IndexEntity> indexNames = new ArrayList<>();
    for (IndexAlians index : indexAnlansList) {
        indexNames.add(indexNamesMap.get(index.getIndex()));
    }
    CommonProps relation = this.commonPropsRepo.queryByKey("templateAndBusinessRelation");
    Map<String, BusinessRelation> patterns = BusinessRelationUtils.getAndCreatePattern(relation);
    indexNames = BusinessRelationUtils.indexClassificationByPattern(indexNames, patterns);

    Map<String, IndexSetting> indexSettingMap = this.indexGlobalSettingCache.getGlobalIndexSetting();
    indexNames = setIndexFreeze(indexNames, indexSettingMap);
    indexNames = this.esAliansService.getindexAlians(indexNames);
    indexNames = setIndexCanWrite(indexNames);
    indexNames = setIndexAlians(indexNames);
    indexNames = setIndexDynamic(indexNames);
    indexNames = setIndexSetting(indexNames);
    return indexNames;
}

public List<IndexEntity> setIndexFreeze(List<IndexEntity> indexNames, Map<String, IndexSetting> indexSettingMap) {
    indexNames.stream().forEach(x -> {
        try {
            String index = x.getIndex();
            IndexSetting indexSetting = indexSettingMap.get(index);
            if (indexSetting == null) {
                indexSetting = this.indexGlobalSettingCache.initOneIndexSetting(index);
            }
            if (indexSetting.getFreeze().booleanValue()) {
                x.setFreeze(Boolean.valueOf(true));
            } else {
                x.setFreeze(Boolean.valueOf(false));
            }
        } catch (Exception e) {
            log.error("获取索引是否冻结失败：{} e:{}", x.getIndex(), e.getMessage(), e);
        }
    });
    return indexNames;
}

public List<IndexEntity> orderData(List<IndexEntity> indexNames, final String orderBy, final String order) {
    if (StringUtils.isBlank(orderBy) || StringUtils.isBlank(order)) {
        indexNames.stream().forEach(x -> x.setDefaultSortField(x.getAlians() + "-" + x.getAlians()));

        indexNames.sort((o1, o2) -> (o1.getDefaultSortField() == null) ? 1 : ((o2.getDefaultSortField() == null) ? -1 : o2.getDefaultSortField().compareTo(o1.getDefaultSortField())));
        return indexNames;
    }

    if (StringUtils.isBlank(orderBy) || StringUtils.isBlank(order)) {
        return indexNames;
    }
    indexNames.sort(new Comparator<IndexEntity>() {
        public int compare(IndexEntity o1, IndexEntity o2) {
            Long o1v, o2v;
            Boolean o1b, o2b;
            int result = 0;
            switch (orderBy) {
                case "index":
                    result = o1.getIndex().compareTo(o2.getIndex());
                    break;
                case "alians":
                    result = o1.getAlians().compareTo(o2.getAlians());
                    break;
                case "code":
                    result = o1.getCode().compareTo(o2.getCode());
                    break;
                case "health":
                    result = o1.getHealth().compareTo(o2.getHealth());
                    break;
                case "status":
                    result = o1.getStatus().compareTo(o2.getStatus());
                    break;
                case "dynamic":
                    result = o1.getDynamic().compareTo(o2.getDynamic());
                    break;
                case "lifecycleStatus":
                    result = o1.getLifecycleStatus().compareTo(o2.getLifecycleStatus());
                    break;
                case "docsCount":
                    result = o1.getDocsCount().compareTo(o2.getDocsCount());
                    break;
                case "storeSizeWithUnit":
                    o1v = Long.valueOf(StringUtils.isBlank(o1.getStoreSize()) ? 0L : Long.parseLong(o1.getStoreSize()));
                    o2v = Long.valueOf(StringUtils.isBlank(o2.getStoreSize()) ? 0L : Long.parseLong(o2.getStoreSize()));
                    result = o1v.compareTo(o2v);
                    break;
                case "pri":
                    if (o1.getPri() == null) {
                        result = -1;
                        break;
                    }
                    if (o2.getPri() == null) {
                        result = 1;
                        break;
                    }
                    result = o1.getPri().compareTo(o2.getPri());
                    break;
                case "indexCanWrite":
                    o1b = Boolean.valueOf((o1.getIndexCanWrite() == null) ? false : o1.getIndexCanWrite().booleanValue());
                    o2b = Boolean.valueOf((o2.getIndexCanWrite() == null) ? false : o2.getIndexCanWrite().booleanValue());
                    result = o1b.compareTo(o2b);
                    break;
            }
            if ("desc".equals(order)) {
                return -result;
            }
            return result;
        }
    });
    return indexNames;
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
    List<String> alinsEx = new ArrayList<>();
    try {
        alinsEx = this.esAliansService.listExceptionAlians();
    } catch (IOException e) {
        log.error("获取异常的别名出错：{}", e.getMessage(), e);
    }
    List<IndexEntity> list = new ArrayList<>(indexNames.size());
    for (IndexEntity x : indexNames) {
        String alians = x.getAlians();
        if (StringUtils.isBlank(alians)) {
            continue;
        }
        if (alinsEx.contains(alians)) {
            list.add(x);
        }
    }
    return list;
}

public List<IndexEntity> setIndexDynamic(List<IndexEntity> indexNames) {
    indexNames.stream().forEach(x -> {
        try {
            String index = x.getIndex();
            String mapping = this.esRestClient.executeGetApi1(index + "/_mapping?include_type_name=true");
            JSONObject obj = JSON.parseObject(mapping);
            String dynamic = (String)JSONPath.eval(obj, "$..mappings._doc.dynamic[0]");
            if (StringUtils.isBlank(dynamic)) {
                dynamic = "true";
            }
            if ("null".equals(dynamic)) {
                dynamic = "true";
            }
            x.setDynamic(dynamic);
        } catch (Exception e) {
            log.error("获取解析索引mapping出错：{} {}", x.getIndex(), e.getMessage(), e);
        }
    });
    return indexNames;
}

public List<IndexEntity> setIndexAlians(List<IndexEntity> indexNames) {
    Map<String, JSONObject> aliansMap = null;
    try {
        aliansMap = this.esAliansService.getAllAliansJson();
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
        String indexSetting = x.getSetting();
        JSONObject indexSettingObj = JSON.parseObject(indexSetting);
        String indexReadOnly = (String)JSONPath.eval(indexSettingObj, "$..settings.index.blocks.read_only[0]");
        if ("true".equals(indexReadOnly)) {
            x.setIndexCanWrite(Boolean.valueOf(false));
            continue;
        }
        String canWrite = (String)JSONPath.eval(indexSettingObj, "$..settings.index.blocks.write[0]");
        if ("false".equals(canWrite)) {
            x.setIndexCanWrite(Boolean.valueOf(true));
        } else if ("true".equals(canWrite)) {
            x.setIndexCanWrite(Boolean.valueOf(false));
        } else {
            x.setIndexCanWrite(Boolean.valueOf(true));
        }
        String aliasResult = x.getAlians();
        int writeCount = this.esAliansService.countAliansWrite(aliasResult);
        x.setAlinasCanWrite(Integer.valueOf(writeCount));
    }
    return indexNames;
}

public List<IndexEntity> filterByAlians(List<IndexEntity> indexNames, String aliansFilter) {
    List<IndexEntity> list = new ArrayList<>(indexNames.size());
    indexNames.stream().forEach(x -> {
        List<String> alians = x.getAliansList();
        if (alians == null || alians.size() < 1) {
            return;
        }
        for (String alinsItem : alians) {
            if (alinsItem.startsWith(aliansFilter)) {
                list.add(x);
            }
        }
    });
    return list;
}

public List<IndexEntity> setIndexSetting(List<IndexEntity> indexNames) {
    indexNames.stream().forEach(x -> x.setSetting(""));
    return indexNames;
}

@Deprecated
private List<IndexEntity> filterByFrozen(List<IndexEntity> indexNames) {
    List<IndexEntity> list = new ArrayList<>();
    indexNames.stream().forEach(x -> {
        try {
            String settings = x.getSetting();
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
        JSONObject obj = (JSONObject)jsonObject;
        String index = obj.getString("index");
        if (StringUtils.isNotBlank(index) && !IndexUtils.isIndexNameContainSpecialChar(index)) {
            String api = index + "/_settings";
            String settings = executeGetApi(api);
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
            String settings = executeGetApi(api);
            item.setSetting(settings);
        }
    }
    return indexNames;
}

public List<BusinessRelation> listBusiness() {
    CommonProps relation = this.commonPropsRepo.queryByKey("templateAndBusinessRelation");
    List<BusinessRelation> list = new ArrayList<>();
    if (relation == null) {
        return list;
    }
    String value = relation.getValue();
    JSONArray json = JSON.parseArray(value);
    json.stream().forEach(jsonObject -> {
        JSONObject obj = (JSONObject)jsonObject;
        String code = obj.getString("code");
        String business = obj.getString("business");
        BusinessRelation businessItem = new BusinessRelation();
        businessItem.setBusiness(business);
        businessItem.setCode(code);
        businessItem.setPattern(null);
        list.add(businessItem);
    });
    BusinessRelation other = new BusinessRelation();
    other.setBusiness("其他");
    other.setCode("other");
    other.setPattern(null);
    list.add(other);
    return list;
}

@Deprecated
public JSONObject closeOneIndex(String index) {
    if (StringUtils.isBlank(index)) {
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.empty"));
    }
    if (index.trim().equals("*")) {
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.close.error"));
    }
    if (index.contains(",")) {
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.close.all.error"));
    }
    if (IndexUtils.isIndexNameContainSpecialChar(index)) {
        log.info("索引包含特殊字符串不予关闭操作");
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.notAllow.close.error"));
    }
    if (!index.contains("-")) {
        log.info("索引不包含横杠不予冻结操作");
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.notAllow.close.error"));
    }
    String[] indes = index.split("-");
    String date = indes[indes.length - 2];
    if (!BusinessRelationUtils.isAllNumber(date)) {
        log.info("索引倒数第二个信息不是数字不予冻结操作");
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.notAllow.close.error"));
    }
    Integer nowDateTime = null;
    String indexParttonFromWeb = null;
    if (date.length() == 4) {
        nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt("yyyy"));
        indexParttonFromWeb = index.substring(0, index.indexOf(date) - 1);
    } else if (date.length() == 6) {
        nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt("yyyyMM"));
        indexParttonFromWeb = index.substring(0, index.indexOf(date) - 1);
    } else if (date.length() == 8) {
        nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt("yyyyMMdd"));
        indexParttonFromWeb = index.substring(0, index.indexOf(date) - 1);
    }
    int indexDateTimeInt = Integer.parseInt(date);
    if (nowDateTime.intValue() < indexDateTimeInt) {
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.error"));
    }
    try {
        List<String> list = listIndexNameByPrefix(indexParttonFromWeb, indexParttonFromWeb + "*");
        if (!list.isEmpty()) {
            String lastIndexName = list.get(0);
            if (index.equals(lastIndexName)) {
                return ResultUtils.onFail(I18nUtils.getMessage("es.index.new.error"));
            }
        }
        JSONObject result = this.esRestClient.closeOneIndex(index);
        return result;
    } catch (Exception e) {
        log.error("关闭索引操作异常：{}", e.getMessage(), e);
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.close.failed") + I18nUtils.getMessage("es.index.close.failed"));
    }
}

@Deprecated
public JSONObject deleteIndex(String index) {
    if (StringUtils.isBlank(index)) {
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.empty"));
    }
    if (index.trim().equals("*")) {
        return ResultUtils.onFail(I18nUtils.getMessage("es.delee.index.all.error"));
    }
    if (index.contains(",")) {
        return ResultUtils.onFail(I18nUtils.getMessage("es.delete.index.all.error"));
    }
    if (IndexUtils.isIndexNameContainSpecialChar(index)) {
        log.info("索引包含特殊字符串不予删除操作");
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.notAllow.del.error"));
    }
    if (!index.contains("-")) {
        log.info("索引不包含横杠不予删除操作");
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.notAllow.del.error"));
    }
    String[] indes = index.split("-");
    String date = indes[indes.length - 2];
    if (!BusinessRelationUtils.isAllNumber(date)) {
        log.info("索引倒数第二个信息不是数字不予删除操作");
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.notAllow.del.error"));
    }
    Integer nowDateTime = null;
    String indexParttonFromWeb = null;
    if (date.length() == 4) {
        nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt("yyyy"));
        indexParttonFromWeb = index.substring(0, index.indexOf(date) - 1) + "-" + index.substring(0, index.indexOf(date) - 1);
    } else if (date.length() == 6) {
        nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt("yyyyMM"));
        indexParttonFromWeb = index.substring(0, index.indexOf(date) - 1) + "-" + index.substring(0, index.indexOf(date) - 1);
    } else if (date.length() == 8) {
        nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt("yyyyMMdd"));
        indexParttonFromWeb = index.substring(0, index.indexOf(date) - 1) + "-" + index.substring(0, index.indexOf(date) - 1);
    }
    int indexDateTimeInt = Integer.parseInt(date);
    try {
        if (nowDateTime.intValue() == indexDateTimeInt) {
            List<String> list = listIndexNameByPrefix(indexParttonFromWeb, indexParttonFromWeb + "*");
            if (!list.isEmpty()) {
                String lastIndexName = list.get(0);
                if (index.equals(lastIndexName)) {
                    return ResultUtils.onFail(I18nUtils.getMessage("es.index.new.notAllow.del.error"));
                }
            }
        }
        JSONObject result = this.esRestClient.deleteIndex(index);
        return result;
    } catch (Exception e) {
        log.error("删除索引操作异常：{}", e.getMessage(), e);
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.delete.failed"));
    }
}

@Deprecated
public boolean isIndexCanWrite(String index) throws IOException {
    String indexSetting = this.esRestClient.executeGetApi1(index + "/_settings");
    JSONObject indexSettingObj = JSON.parseObject(indexSetting);
    String indexReadOnly = (String)JSONPath.eval(indexSettingObj, "$..settings.index.blocks.read_only[/XMLSchemaType]");
    if ("true".equals(indexReadOnly)) {
        return false;
    }
    String canWrite = (String)JSONPath.eval(indexSettingObj, "$..settings.index.blocks.write[0]");
    if ("false".equals(canWrite))
        return true;
    return !"true".equals(canWrite);
}

public JSONObject forceFlushIndexs(String indexs) throws IOException {
    JSONObject response;
    if (indexs == null) {
        return ResultUtils.onFail(I18nUtils.getMessage("params.illegal"));
    }
    String api = indexs + "/_flush";
    String result = this.esRestClient.executePostApi(api, null);
    JSONObject json = JSON.parseObject(result);
    if (json.containsKey("_shards")) {
        Integer failed = json.getJSONObject("_shards").getInteger("failed");
        if (failed.intValue() == 0) {
            response = ResultUtils.onSuccess(I18nUtils.getMessage("es.index.refresh.success"));
        } else {
            response = ResultUtils.onFail(I18nUtils.getMessage("es.index.refresh.error"));
        }
    } else {
        response = ResultUtils.onFail(I18nUtils.getMessage("es.index.refresh.error"));
    }
    return response;
}

public JSONObject forceClearIndexs(String indexs) throws IOException {
    JSONObject response;
    if (indexs == null) {
        return ResultUtils.onFail(I18nUtils.getMessage("params.illegal"));
    }
    String api = indexs + "/_cache/clear";
    String result = this.esRestClient.executePostApi(api, null);
    JSONObject json = JSON.parseObject(result);
    if (json.containsKey("_shards")) {
        Integer failed = json.getJSONObject("_shards").getInteger("failed");
        if (failed.intValue() == 0) {
            response = ResultUtils.onSuccess(I18nUtils.getMessage("es.index.clean.success"));
        } else {
            response = ResultUtils.onFail(I18nUtils.getMessage("es.index.clean.error"));
        }
    } else {
        response = ResultUtils.onFail(I18nUtils.getMessage("es.index.clean.error"));
    }
    return response;
}

public JSONObject forceRefreshIndex(String indexs) throws IOException {
    JSONObject response;
    if (indexs == null) {
        return ResultUtils.onFail(I18nUtils.getMessage("params.illegal"));
    }
    String api = indexs + "/_refresh";
    String result = this.esRestClient.executePostApi(api, null);
    JSONObject json = JSON.parseObject(result);
    if (json.containsKey("_shards")) {
        Integer failed = json.getJSONObject("_shards").getInteger("failed");
        if (failed.intValue() == 0) {
            response = ResultUtils.onSuccess(I18nUtils.getMessage("es.index.refresh.success"));
        } else {
            response = ResultUtils.onFail(I18nUtils.getMessage("es.index.fresh.failed"));
        }
    } else {
        response = ResultUtils.onFail(I18nUtils.getMessage("es.index.fresh.failed"));
    }
    return response;
}

@Deprecated
public String closeIndexs(String indexs) throws IOException {
    String result = this.esRestClient.executePostApi(indexs + "/_close", null);
    if (StringUtils.isNotBlank(indexs)) {
        String[] indexes = indexs.split(",");
        for (String item : indexes) {
            this.indexGlobalSettingCache.initOneIndexSetting(item);
        }
    }
    return result;
}

public String openIndexs(String indexs) throws IOException {
    String result = this.esRestClient.executePostApi(indexs + "/_open", null);
    if (StringUtils.isNotBlank(indexs)) {
        String[] indexes = indexs.split(",");
        for (String item : indexes) {
            this.indexGlobalSettingCache.initOneIndexSetting(item);
        }
    }
    return result;
}

@Deprecated
public String delIndexs(String indexs) throws IOException {
    String result = this.esRestClient.executeDelApi1(indexs, null);
    if (StringUtils.isNotBlank(indexs)) {
        String[] indexes = indexs.split(",");
        for (String item : indexes) {
            this.indexGlobalSettingCache.initOneIndexSetting(item);
        }
    }
    return result;
}

@Deprecated
public String frozenIndexs(String indexs) throws IOException {
    String result = this.esRestClient.executePostApi(indexs + "/_freeze", null);
    if (StringUtils.isNotBlank(indexs)) {
        String[] indexes = indexs.split(",");
        for (String item : indexes) {
            this.indexGlobalSettingCache.initOneIndexSetting(item);
        }
    }
    return result;
}

public String unFrozenIndexs(String indexs) throws IOException {
    String result = this.esRestClient.executePostApi(indexs + "/_unfreeze", null);
    if (StringUtils.isNotBlank(indexs)) {
        String[] indexes = indexs.split(",");
        for (String item : indexes) {
            this.indexGlobalSettingCache.initOneIndexSetting(item);
        }
    }
    return result;
}

public String segmentForceMerge(String index) throws IOException {
    if (StringUtils.isBlank(index)) {
        return "索引信息为空";
    }
    String result = this.esRestClient.executePostApi(index + "/_forcemerge", null);
    return result;
}

public String getClusterHealth() {
    String result = executeGetApi(EsRestApi.CLUSTER_HEALTH.getApi());
    return result;
}

public List<IndexEntity> listIndexByPrefix(boolean setting, String indexPrefxi, String indexXing) throws IOException {
    String api = "/_cat/indices/" + indexXing + "?format=json";
    log.info("获取某种类型的索引：{}", api);
    String res1 = this.esRestClient.executeGetApi1(api);
    JSONArray jsonArray = JSON.parseArray(res1);

    List<JSONObject> list = new ArrayList<>();
    jsonArray.stream().forEach(jsonObject -> {
        JSONObject obj = (JSONObject)jsonObject;
        String index = obj.getString("index");
        if (index.startsWith(indexPrefxi)) {
            list.add(obj);
        }
    });
    log.info("某种前缀的索引个数：{}", Integer.valueOf(list.size()));

    List<List<JSONObject>> indexList = ListUtils.splitList(list, 200);
    if (indexList == null) {
        return Collections.emptyList();
    }

    List<Future<Map<String, IndexEntity>>> featureList = new ArrayList<>();
    for (List<JSONObject> childList : indexList) {
        log.info("多线程提交获取索引的设置：{}", Integer.valueOf(childList.size()));
        this.traceIdKd.set("thread-get-index-setting");
        ListenableFuture listenableFuture = this.executorService.submit((Callable)new IndexSettingCallable(childList, setting, this.esRestClient, this.traceIdKd));
        featureList.add(listenableFuture);
        this.traceIdKd.remove();
    }

    List<IndexEntity> allResult = new ArrayList<>();
    Iterator<Future<Map<String, IndexEntity>>> iterator = featureList.iterator();
    while (iterator.hasNext()) {
        Future<Map<String, IndexEntity>> childTask = iterator.next();
        try {
            if (childTask == null) {
                continue;
            }
            Map<String, IndexEntity> result = childTask.get(40L, TimeUnit.SECONDS);
            if (result != null) {
                for (Map.Entry<String, IndexEntity> item : result.entrySet()) {
                    allResult.add(item.getValue());
                }
            }
        } catch (Exception e) {
            log.error("从多线程中获取broker jmx 执行结果异常：{} feature:{} isCancelled:{}", new Object[] { e.getMessage(), Boolean.valueOf(childTask.isDone()), Boolean.valueOf(childTask.isCancelled()), e });
        }
    }

    log.info("获取多线程执行的总结果：{}", Integer.valueOf(allResult.size()));
    return allResult;
}

public List<String> listIndexNameByPrefix(String indexPrefxi, String indexXing) throws IOException {
    String api = "/_cat/indices/" + indexXing + "?format=json";
    log.info("获取某种类型的索引：{}", api);
    String res1 = this.esRestClient.executeGetApi1(api);
    JSONArray jsonArray = JSON.parseArray(res1);

    List<String> list = new ArrayList<>();
    Iterator<Object> iterator = jsonArray.iterator();
    while (iterator.hasNext()) {
        JSONObject obj = (JSONObject)iterator.next();
        String index = obj.getString("index");
        if (index.startsWith(indexPrefxi)) {
            list.add(index);
        }
    }

    list.sort((o1, o2) -> o2.compareTo(o1));
    log.info("获取前缀为{} 索引个数总结果：{}", indexPrefxi, Integer.valueOf(list.size()));
    return list;
}

public Map<String, IndexSetting> getGlobalIndexSettingFromEs() throws IOException {
    String result = this.esRestClient.executeGetApi1("/*/_settings");
    if (StringUtils.isBlank(result)) {
        return Collections.emptyMap();
    }

    JSONObject setting = JSON.parseObject(result);
    Map<String, IndexSetting> indexMap = new HashMap<>(1000);
    for (Map.Entry<String, Object> item : (Iterable<Map.Entry<String, Object>>)setting.entrySet()) {
        String index = item.getKey();
        JSONObject jsonObjectSetting = (JSONObject)item.getValue();

        IndexSetting indexSetting = parseIndexSetting(index, jsonObjectSetting);

        indexMap.put(index, indexSetting);
    }
    return indexMap;
}

private IndexSetting parseIndexSetting(String index, JSONObject jsonObjectSetting) {
    IndexSetting indexSetting = new IndexSetting();
    indexSetting.setSettingData(jsonObjectSetting);
    indexSetting.setIndex(index);

    Object frozen = JSONPath.eval(jsonObjectSetting, "$.settings.index.frozen");
    if (frozen == null) {
        indexSetting.setFreeze(Boolean.valueOf(false));
    } else {
        String frozenStr = (String)frozen;
        if ("true".equals(frozenStr)) {
            indexSetting.setFreeze(Boolean.valueOf(true));
        } else {
            indexSetting.setFreeze(Boolean.valueOf(false));
        }
    }

    String canWrite = (String)JSONPath.eval(jsonObjectSetting, "$.settings.index.blocks.write[0]");
    if ("false".equals(canWrite)) {
        indexSetting.setBlocksWrite(Boolean.valueOf(false));
    } else if ("true".equals(canWrite)) {
        indexSetting.setBlocksWrite(Boolean.valueOf(true));
    } else {
        indexSetting.setBlocksWrite(Boolean.valueOf(false));
    }
    return indexSetting;
}

public IndexSetting initOneIndexSetting(String index) {
    String result = null;
    try {
        result = this.esRestClient.executeGetApi1("/" + index + "/_settings");
    } catch (IOException e) {
        log.error("获取{}索引的setting异常：{}", new Object[] { index, e.getMessage(), e });
    }
    if (StringUtils.isBlank(result)) {
        return null;
    }

    JSONObject setting = JSON.parseObject(result);
    JSONObject jsonObjectSetting = setting.getJSONObject(index);
    IndexSetting indexSetting = parseIndexSetting(index, jsonObjectSetting);
    return indexSetting;
}

@Deprecated
public List<String> getIndexCanClose(String indexs) throws IOException {
    String[] indexNames = indexs.split(",");
    List<String> canFreezeIndex = new ArrayList<>();
    for (String indexName : indexNames) {
        if (IndexUtils.isIndexNameContainSpecialChar(indexName)) {
            log.error("索引：{}包含特殊字符串不予关闭操作", indexName);
        }
        else if (!indexName.contains("-")) {
            log.error("索引：{}不包含横杠不予关闭操作", indexName);
        }
        else {
            String[] indes = indexName.split("-");
            String date = indes[indes.length - 2];

            if (!BusinessRelationUtils.isAllNumber(date)) {
                log.error("索引：{}倒数第二个信息不是数字不予关闭操作", indexName);
            }
            else {
                Integer nowDateTime = null;
                if (date.length() == 4) {
                    nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt("yyyy"));
                } else if (date.length() == 6) {
                    nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt("yyyyMM"));
                } else if (date.length() == 8) {
                    nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt("yyyyMMdd"));
                }

                int indexDateTimeInt = Integer.parseInt(date);

                if (nowDateTime.intValue() <= indexDateTimeInt) {
                    log.error("索引：{}是今天或者为了索引不予关闭操作", indexName);
                }
                else if (!isIndexOpen(indexName)) {
                    log.error("索引：{}关闭状态不予关闭操作", indexName);
                }
                else {
                    canFreezeIndex.add(indexName);
                }
            }
        }
    }
    return canFreezeIndex;
}

public boolean isIndexOpen(String indexName) throws IOException {
    String result = this.esRestClient.executeGetApi1("/_cat/indices/" + indexName + "?format=json");
    JSONArray array = JSONArray.parseArray(result);
    JSONObject obj = array.getJSONObject(0);
    String status = obj.getString("status");
    return "open".equals(status);
}

private boolean isIndexClose(String index) throws IOException {
    String result = this.esRestClient.executeGetApi1("/_cat/indices/" + index + "?format=json");
    JSONArray array = JSONArray.parseArray(result);
    JSONObject obj = array.getJSONObject(0);
    String status = obj.getString("status");
    return "close".equals(status);
}

private boolean isIndexFreeze(String indexName) {
    Map<String, IndexSetting> indexSettingMap = this.indexGlobalSettingCache.getGlobalIndexSetting();
    IndexSetting indexSetting = indexSettingMap.get(indexName);
    if (indexSetting == null) {
        indexSetting = this.indexGlobalSettingCache.initOneIndexSetting(indexName);
    }
    if (indexSetting.getFreeze().booleanValue()) {
        return true;
    }
    return false;
}

public JSONObject indexOperator(String index, IndexOperatorType indexOperatorType) {
    if (index.startsWith("ailpha-baas-alarm-"))
        return ailphaBaasAlarmOperator(index, indexOperatorType);
    if (index.startsWith("ailpha-baas-event-"))
        return ailphaBaasEventOperator(index, indexOperatorType);
    if (index.startsWith("ailpha-baas-flow-"))
        return ailphaBaasFlowOperator(index, indexOperatorType);
    if (index.startsWith("ailpha-baas-log-"))
        return ailphaBaasLogOperator(index, indexOperatorType);
    if (index.startsWith("ailpha-custom-alarm-"))
        return ailphaCustomeAlarmOperator(index, indexOperatorType);
    if (index.startsWith("ailpha-statistics-all-")) {
        return ailphaStaticAllOperator1(index, indexOperatorType);
    }
    if (index.startsWith("ailpha-statistics-custom-")) {
        return ailphaStaticCustomeOperator1(index, indexOperatorType);
    }
    if (index.startsWith("threat_intelligence_lib_data"))
        return ailphaIntelligenceLibDataOperator(index, indexOperatorType);
    if (index.startsWith("."))
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.notAllow.failed"));
    if (index.startsWith("ailpha-sended-alarm")) {
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.notAllow.failed"));
    }
    return canDoAllOperator(index, indexOperatorType);
}

private JSONObject ailphaStaticCustomeOperator1(String index, IndexOperatorType indexOperatorType) {
    String indexPrefix = "ailpha-statistics-custom-";
    String format = this.esTemplateService.getTemplateDatePattern("ailpha-statistics-custom");

    Boolean feature = isIndexFeature(index, indexPrefix, format);
    Boolean indexNow = isIndexNow(index, indexPrefix, format);
    if (feature.booleanValue()) {
        if (indexOperatorType == IndexOperatorType.CLOSE) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.close.failed"));
        }
        if (indexOperatorType == IndexOperatorType.OPEN) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.open.failed"));
        }
        if (indexOperatorType == IndexOperatorType.FREEZE) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.freeze.failed"));
        }
    }

    if (indexNow.booleanValue()) {
        String indexParttonFromWeb = getIndexNowPrefix(indexPrefix, format);
        List<String> list = new ArrayList<>(0);
        try {
            list = listIndexNameByPrefix(indexParttonFromWeb, indexParttonFromWeb + "*");
        } catch (Exception e) {
            log.error("根据索引前缀{}获取索引异常：{}", new Object[] { indexParttonFromWeb, e.getMessage(), e });
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.search.failed"));
        }
        if (!list.isEmpty()) {
            String lastIndexName = list.get(0);
            if (index.equals(lastIndexName)) {
                return ResultUtils.onFail(I18nUtils.getMessage("es.index.new.operate.failed"));
            }
        }
    }
    return canDoAllOperator(index, indexOperatorType);
}

private JSONObject ailphaStaticAllOperator1(String index, IndexOperatorType indexOperatorType) {
    String indexPrefix = "ailpha-statistics-all-";
    String format = this.esTemplateService.getTemplateDatePattern("ailpha-statistics-all");

    Boolean feature = isIndexFeature(index, indexPrefix, format);
    Boolean indexNow = isIndexNow(index, indexPrefix, format);
    if (feature.booleanValue()) {
        if (indexOperatorType == IndexOperatorType.CLOSE) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.close.failed"));
        }
        if (indexOperatorType == IndexOperatorType.OPEN) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.open.failed"));
        }
        if (indexOperatorType == IndexOperatorType.FREEZE) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.freeze.failed"));
        }
    }

    if (indexNow.booleanValue()) {
        String indexParttonFromWeb = getIndexNowPrefix(indexPrefix, format);
        List<String> list = new ArrayList<>(0);
        try {
            list = listIndexNameByPrefix(indexParttonFromWeb, indexParttonFromWeb + "*");
        } catch (Exception e) {
            log.error("根据索引前缀{}获取索引异常：{}", new Object[] { indexParttonFromWeb, e.getMessage(), e });
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.search.failed"));
        }
        if (!list.isEmpty()) {
            String lastIndexName = list.get(0);
            if (index.equals(lastIndexName)) {
                return ResultUtils.onFail(I18nUtils.getMessage("es.index.new.operate.failed"));
            }
        }
    }
    return canDoAllOperator(index, indexOperatorType);
}

private JSONObject ailphaIntelligenceLibDataOperator(String index, IndexOperatorType indexOperatorType) {
    return ResultUtils.onFail(I18nUtils.getMessage("es.index.notAllow.failed"));
}

private JSONObject ailphaStaticCustomeOperator(String index, IndexOperatorType indexOperatorType) {
    String indexPrefix = "ailpha-statistics-custom-";

    int nowYear = EsDateUtils.getNowDateInt("yyyy");
    int nowMonthInt = EsDateUtils.getNowDateInt("yyyyMMdd");
    int preSevenEnd = Integer.parseInt("" + nowYear - 1 + "1232");
    int oneEnd = Integer.parseInt("" + nowYear + "0632");
    int sevenEnd = Integer.parseInt("" + nowYear + "1232");

    String indexParttonFromWeb = null;
    if (preSevenEnd < nowMonthInt && nowMonthInt < oneEnd) {
        indexParttonFromWeb = indexPrefix + indexPrefix + "01";
    }
    if (oneEnd < nowMonthInt && nowMonthInt < sevenEnd) {
        indexParttonFromWeb = indexPrefix + indexPrefix + "07";
    }

    List<String> list = new ArrayList<>(0);
    try {
        list = listIndexNameByPrefix(indexParttonFromWeb, indexParttonFromWeb + "*");
    } catch (Exception e) {
        log.error("根据索引前缀{}获取索引异常：{}", new Object[] { indexParttonFromWeb, e.getMessage(), e });
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.search.failed"));
    }
    if (!list.isEmpty()) {
        String lastIndexName = list.get(0);
        if (index.equals(lastIndexName)) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.new.operate.failed"));
        }
    }
    return canDoAllOperator(index, indexOperatorType);
}

private JSONObject ailphaStaticAllOperator(String index, IndexOperatorType indexOperatorType) {
    String indexPrefix = "ailpha-statistics-all-";

    int nowYear = EsDateUtils.getNowDateInt("yyyy");
    int nowMonthInt = EsDateUtils.getNowDateInt("yyyyMMdd");
    int preSevenEnd = Integer.parseInt("" + nowYear - 1 + "1232");
    int oneEnd = Integer.parseInt("" + nowYear + "0632");
    int sevenEnd = Integer.parseInt("" + nowYear + "1232");

    String indexParttonFromWeb = null;
    if (preSevenEnd < nowMonthInt && nowMonthInt < oneEnd) {
        indexParttonFromWeb = indexPrefix + indexPrefix + "01";
    }
    if (oneEnd < nowMonthInt && nowMonthInt < sevenEnd) {
        indexParttonFromWeb = indexPrefix + indexPrefix + "07";
    }

    List<String> list = new ArrayList<>(0);
    try {
        list = listIndexNameByPrefix(indexParttonFromWeb, indexParttonFromWeb + "*");
    } catch (Exception e) {
        log.error("根据索引前缀{}获取索引异常：{}", new Object[] { indexParttonFromWeb, e.getMessage(), e });
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.search.failed"));
    }
    if (!list.isEmpty()) {
        String lastIndexName = list.get(0);
        if (index.equals(lastIndexName)) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.new.operate.failed"));
        }
    }
    return canDoAllOperator(index, indexOperatorType);
}

private JSONObject ailphaCustomeAlarmOperator(String index, IndexOperatorType indexOperatorType) {
    List<String> list = new ArrayList<>(0);
    try {
        list = listIndexNameByPrefix("ailpha-custom-alarm-", "ailpha-custom-alarm-*");
    } catch (Exception e) {
        log.error("根据索引前缀{}获取索引异常：ailpha-custom-alarm- {} ", e.getMessage(), e);
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.search.failed"));
    }
    if (!list.isEmpty()) {
        String lastIndexName = list.get(0);
        if (index.equals(lastIndexName)) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.new.operate.failed"));
        }
    }
    return canDoAllOperator(index, indexOperatorType);
}

private JSONObject ailphaBaasLogOperator(String index, IndexOperatorType indexOperatorType) {
    String indexPrefix = "ailpha-baas-log-";
    String format = this.esTemplateService.getTemplateDatePattern("ailpha-baas-log");
    Boolean feature = isIndexFeature(index, indexPrefix, format);
    Boolean indexNow = isIndexNow(index, indexPrefix, format);
    if (feature.booleanValue()) {
        if (indexOperatorType == IndexOperatorType.CLOSE) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.close.failed"));
        }
        if (indexOperatorType == IndexOperatorType.OPEN) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.open.failed"));
        }
        if (indexOperatorType == IndexOperatorType.FREEZE) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.freeze.failed"));
        }
    }

    if (indexNow.booleanValue()) {
        String indexParttonFromWeb = getIndexNowPrefix(indexPrefix, format);
        List<String> list = new ArrayList<>(0);
        try {
            list = listIndexNameByPrefix(indexParttonFromWeb, indexParttonFromWeb + "*");
        } catch (Exception e) {
            log.error("根据索引前缀{}获取索引异常：{}", new Object[] { indexParttonFromWeb, e.getMessage(), e });
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.search.failed"));
        }
        if (!list.isEmpty()) {
            String lastIndexName = list.get(0);
            if (index.equals(lastIndexName)) {
                return ResultUtils.onFail(I18nUtils.getMessage("es.index.new.operate.failed"));
            }
        }
    }
    return canDoAllOperator(index, indexOperatorType);
}

private JSONObject ailphaBaasFlowOperator(String index, IndexOperatorType indexOperatorType) {
    Boolean feature = isIndexFeature(index, "ailpha-baas-flow-", "yyyyMMdd");
    Boolean indexNow = isIndexNow(index, "ailpha-baas-flow-", "yyyyMMdd");
    if (feature.booleanValue()) {
        if (indexOperatorType == IndexOperatorType.CLOSE) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.close.failed"));
        }
        if (indexOperatorType == IndexOperatorType.OPEN) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.open.failed"));
        }
        if (indexOperatorType == IndexOperatorType.FREEZE) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.freeze.failed"));
        }
    }

    if (indexNow.booleanValue()) {
        String indexParttonFromWeb = getIndexNowPrefix("ailpha-baas-flow-", "yyyyMMdd");
        List<String> list = new ArrayList<>(0);
        try {
            list = listIndexNameByPrefix(indexParttonFromWeb, indexParttonFromWeb + "*");
        } catch (Exception e) {
            log.error("根据索引前缀{}获取索引异常：{}", new Object[] { indexParttonFromWeb, e.getMessage(), e });
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.search.failed"));
        }
        if (!list.isEmpty()) {
            String lastIndexName = list.get(0);
            if (index.equals(lastIndexName)) {
                return ResultUtils.onFail(I18nUtils.getMessage("es.index.new.operate.failed"));
            }
        }
    }
    return canDoAllOperator(index, indexOperatorType);
}

private JSONObject ailphaBaasEventOperator(String index, IndexOperatorType indexOperatorType) {
    Boolean feature = isIndexFeature(index, "ailpha-baas-event-", "yyyyMM");
    Boolean indexNow = isIndexNow(index, "ailpha-baas-event-", "yyyyMM");
    if (feature.booleanValue()) {
        if (indexOperatorType == IndexOperatorType.CLOSE) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.close.failed"));
        }
        if (indexOperatorType == IndexOperatorType.OPEN) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.open.failed"));
        }
        if (indexOperatorType == IndexOperatorType.FREEZE) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.freeze.failed"));
        }
    }

    if (indexNow.booleanValue()) {
        String indexParttonFromWeb = getIndexNowPrefix("ailpha-baas-event-", "yyyyMM");
        List<String> list = new ArrayList<>(0);
        try {
            list = listIndexNameByPrefix(indexParttonFromWeb, indexParttonFromWeb + "*");
        } catch (Exception e) {
            log.error("根据索引前缀{}获取索引异常：{}", new Object[] { indexParttonFromWeb, e.getMessage(), e });
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.search.failed"));
        }
        if (!list.isEmpty()) {
            String lastIndexName = list.get(0);
            if (index.equals(lastIndexName)) {
                return ResultUtils.onFail(I18nUtils.getMessage("es.index.new.operate.failed"));
            }
        }
    }
    return canDoAllOperator(index, indexOperatorType);
}

private JSONObject ailphaBaasAlarmOperator(String index, IndexOperatorType indexOperatorType) {
    Boolean feature = isIndexFeature(index, "ailpha-baas-alarm-", "yyyy");
    Boolean indexNow = isIndexNow(index, "ailpha-baas-alarm-", "yyyy");
    if (feature.booleanValue()) {
        if (indexOperatorType == IndexOperatorType.CLOSE) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.close.failed"));
        }
        if (indexOperatorType == IndexOperatorType.OPEN) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.open.failed"));
        }
        if (indexOperatorType == IndexOperatorType.FREEZE) {
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.feature.freeze.failed"));
        }
    }

    if (indexNow.booleanValue()) {
        String indexParttonFromWeb = getIndexNowPrefix("ailpha-baas-alarm-", "yyyy");
        List<String> list = new ArrayList<>(0);
        try {
            list = listIndexNameByPrefix(indexParttonFromWeb, indexParttonFromWeb + "*");
        } catch (Exception e) {
            log.error("根据索引前缀{}获取索引异常：{}", new Object[] { indexParttonFromWeb, e.getMessage(), e });
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.search.failed"));
        }
        if (!list.isEmpty()) {
            String lastIndexName = list.get(0);
            if (index.equals(lastIndexName)) {
                return ResultUtils.onFail(I18nUtils.getMessage("es.index.new.operate.failed"));
            }
        }
    }
    return canDoAllOperator(index, indexOperatorType);
}

private JSONObject canDoAllOperator(String index, IndexOperatorType indexOperatorType) {
    if (StringUtils.isBlank(index)) {
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.empty"));
    }
    if (index.trim().equals("*")) {
        return ResultUtils.onFail(I18nUtils.getMessage("es.index.close.error"));
    }
    try {
        boolean indexFreeze = isIndexFreeze(index);
        boolean indexOpen = isIndexOpen(index);
        boolean indexClose = isIndexClose(index);
        if (indexOperatorType == IndexOperatorType.OPEN) {
            if (indexOpen) {
                return ResultUtils.onSuccess(I18nUtils.getMessage("es.index.open.status"));
            }
            String result = openIndexs(index);
            JSONObject json = JSON.parseObject(result);
            if (json.containsKey("acknowledged") && json.getBooleanValue("acknowledged")) {
                log.info("开启索引:" + index + "操作成功");
                return ResultUtils.onSuccess(ResultEnum.SUCCESS.msg);
            }
            log.info("开启索引:" + index + "操作失败");
            return ResultUtils.onFail(I18nUtils.getMessage("es.index.open.error"));
        }
        if (indexOperatorType == IndexOperatorType.CLOSE) {
            if (indexClose) {
                return ResultUtils.onSuccess(I18nUtils.getMessage("es.index.close.status"));
            }
            JSONObject json = this.esRestClient.closeOneIndex(index);
            return json;
        }
        if (indexOperatorType == IndexOperatorType.DELETE) {
            JSONObject result = this.esRestClient.deleteIndex(index);
            return result;
        }
        log.error("没有操作类型");
    }
    catch (Exception e) {
        log.error("获取索引状态信息异常：{}", index, e);
    }
    return ResultUtils.onFail(I18nUtils.getMessage("es.index.operate.error"));
}

private String getIndexNowPrefix(String prefix, String format) {
    String indexParttonFromWeb = null;
    int nowDateTime = EsDateUtils.getNowDateInt(format);
    if ("yyyy".equals(format)) {
        indexParttonFromWeb = prefix + prefix;
    } else if ("yyyyMM".equals(format)) {
        indexParttonFromWeb = prefix + prefix;
    } else if ("yyyyMMdd".equals(format)) {
        indexParttonFromWeb = prefix + prefix;
    }
    return indexParttonFromWeb;
}

private Boolean isIndexFeature(String index, String prefix, String format) {
    Integer nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt(format));
    String[] indes = index.split("-");
    String date = indes[indes.length - 2];
    if (!BusinessRelationUtils.isAllNumber(date)) {
        log.error("索引：{}倒数第二个信息不是数字不予操作", index);
        return Boolean.valueOf(false);
    }
    int indexDateTimeInt = Integer.parseInt(date);
    return Boolean.valueOf((nowDateTime.intValue() < indexDateTimeInt));
}

private Boolean isIndexNow(String index, String prefix, String format) {
    Integer nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt(format));
    String[] indes = index.split("-");
    String date = indes[indes.length - 2];
    if (!BusinessRelationUtils.isAllNumber(date)) {
        log.error("索引：{}倒数第二个信息不是数字不予操作", index);
        return Boolean.valueOf(false);
    }
    int indexDateTimeInt = Integer.parseInt(date);
    return Boolean.valueOf((nowDateTime.intValue() == indexDateTimeInt));
}

public List<IndexEntity> filterIndexByPattern(List<IndexEntity> indexList, String indexPrefixNoDate, String dateFormat, String pattern) {
    if (StringUtils.isBlank(pattern))
    {
        pattern = indexPrefixNoDate + "[0-9]{" + indexPrefixNoDate + "}-[0-9]{6}";
    }
    log.info("根据正则过滤索引：indexPrefixNoDate:{} \t dateFormat:{}  \t {}", new Object[] { indexPrefixNoDate, dateFormat, pattern });
    Pattern patternMatcher = Pattern.compile(pattern);

    List<IndexEntity> result = new ArrayList<>(indexList.size());
    for (IndexEntity indexEntity : indexList) {
        String index = indexEntity.getIndex();
        if (patternMatcher.matcher(index).find()) {
            result.add(indexEntity);
        }
    }
    return result;
}
