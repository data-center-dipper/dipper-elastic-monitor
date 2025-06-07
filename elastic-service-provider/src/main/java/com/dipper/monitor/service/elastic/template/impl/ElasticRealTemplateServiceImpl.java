package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.life.EsTemplateConfigMes;
import com.dipper.monitor.entity.elastic.template.history.EsTemplateInfo;
import com.dipper.monitor.entity.elastic.template.history.TemplateDetailView;
import com.dipper.monitor.entity.elastic.template.history.TemplateHistoryView;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.life.ElasticRealLifecyclePoliciesService;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.service.elastic.segment.ElasticSegmentService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.service.elastic.template.TemplatePreviewService;
import com.dipper.monitor.service.elastic.template.impl.handlers.RollingIndexByTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.StatTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.version.AbstractRealTemplateService;
import com.dipper.monitor.utils.elastic.ClusterVersionUtils;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.nio.entity.NStringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * // 可选接口 GET /_cat/templates?format=json
 * // 获取模版详情 GET /_index_template/ailpha-logs-20250517
 * // 获取模版，包含详情信息  GET /_index_template
 */
@Slf4j
@Service
public class ElasticRealTemplateServiceImpl extends AbstractRealTemplateService implements ElasticRealTemplateService {

    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticRealIndexService elasticRealIndexService;
    @Autowired
    private ElasticShardService elasticShardService;
    @Autowired
    private ElasticSegmentService elasticSegmentService;
    @Autowired
    private ElasticRealLifecyclePoliciesService elasticRealLifecyclePoliciesService;
    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;
    @Autowired
    private TemplatePreviewService templatePreviewService;
    @Autowired
    private ElasticHealthService elasticHealthService;


    private Cache<String, List<TemplateHistoryView>> cacheTemplate;
    private static final String TEMPLATE_KEY = "template_key";

    @PostConstruct
    public void init() {
        this.cacheTemplate = CacheBuilder.newBuilder()
                .maximumSize(7L)
                .expireAfterWrite(10L, TimeUnit.MINUTES)
                .concurrencyLevel(7)
                .build();
    }


    public boolean isExistTemplate(String name) throws IOException {
        String api = "/_template/" + name;
        boolean b = elasticClientService.executeHeadApi(api);
        return b;
    }

    public boolean saveOrUpdateTemplate(String name, JSONObject templateJson) {
        try {
            String method = isExistTemplate(name) ? "POST" : "PUT";
            NStringEntity nStringEntity = new NStringEntity(templateJson.toJSONString());
            Response response = null;
            CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
            String clusterVersion = currentCluster.getClusterVersion();
            if (ClusterVersionUtils.is7xVersion(clusterVersion)) {
                if ("POST".equalsIgnoreCase(method)) {
                    response = elasticClientService.executePostApiReturnResponse("/_template/" + name, nStringEntity);
                } else {
                    response = elasticClientService.executePutApiReturnResponse("/_template/" + name, nStringEntity);
                }
            } else if (ClusterVersionUtils.is8xVersion(clusterVersion)) {
                if ("POST".equalsIgnoreCase(method)) {
                    response = elasticClientService.executePostApiReturnResponse("/_index_template/" + name, nStringEntity);
                } else {
                    response = elasticClientService.executePutApiReturnResponse("/_index_template/" + name, nStringEntity);
                }
            } else {
                throw new IllegalArgumentException("不支持的集群版本");
            }

            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
            log.error("索引模板创建失败：{}", response);
            return false;
        } catch (Exception e) {
            log.error("索引模板创建失败：{}", templateJson, e);
            return false;
        }
    }

    /**
     * 点开某个模版 查看模版的详情
     *
     * @param name
     * @return
     * @throws IOException
     */
    @Override
    public List<EsTemplateConfigMes> statTemplate(String name) throws IOException {
        StatTemplateHandler statTemplateHandler = new StatTemplateHandler(elasticClientService,
                elasticRealIndexService, elasticShardService,
                elasticSegmentService, elasticRealLifecyclePoliciesService);
        return statTemplateHandler.statTemplate(name);
    }


    @Override
    public List<String> getIndexPatternList(String indexPattern) {
        List<String> indexPatternList = new ArrayList<>();
        String indexPrefxi = null;
        if (indexPattern.contains("yyyy")) {
            indexPrefxi = indexPattern.substring(0, indexPattern.indexOf("yyyy"));
        } else {
            int position = indexPattern.indexOf("*");
            if (position < 1) {
                position = indexPattern.length();
            }
            indexPrefxi = indexPattern.substring(0, position);
        }
        indexPatternList.add(indexPrefxi);
        return indexPatternList;
    }

    @Override
    public JSONObject rollTemplate(Integer id) throws Exception {
        EsUnconvertedTemplate oneUnconvertedTemplate = elasticStoreTemplateService.getOneUnconvertedTemplate(id);
//        JSONObject jsonObject = templatePreviewService.previewEffectTemplate(id);
        if (oneUnconvertedTemplate == null) {
            throw new RuntimeException("模板不存在");
        }

        RollingIndexByTemplateHandler rollingIndexByTemplateHandler = new RollingIndexByTemplateHandler(elasticHealthService,
                elasticStoreTemplateService, templatePreviewService);
        rollingIndexByTemplateHandler.rollIndexByTemplate(oneUnconvertedTemplate);
        return null;
    }

    // 可选接口 GET /_cat/templates?format=json
    @Override
    public List<EsTemplateInfo> getTemplateList() throws IOException {
        String api = "/_cat/templates?format=json";
        String responseJson = elasticClientService.executeGetApi(api);

        // 使用 FastJSON 解析 JSON 字符串到 Java 对象列表
        List<EsTemplateInfo> templateInfoList = JSON.parseObject(
                responseJson,
                new TypeReference<List<EsTemplateInfo>>() {
                }
        );

        return templateInfoList;
    }

    // 获取模版，包含详情信息  GET /_index_template
    @Override
    public List<TemplateDetailView> getTemplateDetailListByNewApi() throws IOException {
        String api = "/_index_template";
        String response = elasticClientService.executeGetApi(api);
        JSONObject jsonObject = JSONObject.parseObject(response);

        JSONArray templatesArray = jsonObject.getJSONArray("index_templates");
        if (templatesArray == null || templatesArray.isEmpty()) {
            return new ArrayList<>();
        }

        List<TemplateDetailView> result = new ArrayList<>();

        for (int i = 0; i < templatesArray.size(); i++) {
            JSONObject templateObj = templatesArray.getJSONObject(i);
            if (templateObj == null) continue;

            String name = templateObj.getString("name");

            JSONObject indexTemplate = templateObj.getJSONObject("index_template");
            if (indexTemplate == null) continue;

            JSONArray indexPatternsArray = indexTemplate.getJSONArray("index_patterns");
            List<String> indexPatterns = indexPatternsArray != null ?
                    indexPatternsArray.toJavaList(String.class) : new ArrayList<>();

            Integer priority = null;
            if (indexTemplate.containsKey("priority")) {
                priority = indexTemplate.getInteger("priority");
            }

            JSONObject template = indexTemplate.getJSONObject("template");
            JSONObject settingsObj = null;
            if (template != null && template.containsKey("settings")) {
                settingsObj = template.getJSONObject("settings");
            }

            JSONObject settings = null;
            if (settingsObj != null && settingsObj.containsKey("index")) {
                settings = settingsObj.getJSONObject("index");
            }

            String lifecycleName = null;
            String rolloverAlias = null;
            if (settings != null && settings.containsKey("lifecycle")) {
                JSONObject lifecycle = settings.getJSONObject("lifecycle");
                if (lifecycle != null) {
                    lifecycleName = lifecycle.getString("name");
                    rolloverAlias = lifecycle.getString("rollover_alias");
                }
            }

            String numberOfShards = null;
            if (settings != null && settings.containsKey("number_of_shards")) {
                numberOfShards = settings.getString("number_of_shards");
            }

            String numberOfReplicas = null;
            if (settings != null && settings.containsKey("number_of_replicas")) {
                numberOfReplicas = settings.getString("number_of_replicas");
            }

            String refreshInterval = null;
            if (settings != null && settings.containsKey("refresh_interval")) {
                refreshInterval = settings.getString("refresh_interval");
            }

            String codec = null;
            if (settings != null && settings.containsKey("codec")) {
                codec = settings.getString("codec");
            }

            String mode = null;
            if (settings != null && settings.containsKey("mode")) {
                mode = settings.getString("mode");
            }

            String ignoreMalformed = null;
            if (settings != null && settings.containsKey("mapping") &&
                    settings.getJSONObject("mapping").containsKey("ignore_malformed")) {
                ignoreMalformed = settings.getJSONObject("mapping").getString("ignore_malformed");
            }

            String slowIndexThreshold = null;
            if (settings != null && settings.containsKey("indexing") &&
                    settings.getJSONObject("indexing").containsKey("slowlog") &&
                    settings.getJSONObject("indexing").getJSONObject("slowlog").containsKey("threshold") &&
                    settings.getJSONObject("indexing").getJSONObject("slowlog").getJSONObject("threshold").containsKey("index")) {
                slowIndexThreshold = settings.getJSONObject("indexing")
                        .getJSONObject("slowlog")
                        .getJSONObject("threshold")
                        .getJSONObject("index")
                        .getString("info");
            }

            String slowFetchThreshold = null;
            if (settings != null && settings.containsKey("search") &&
                    settings.getJSONObject("search").containsKey("slowlog") &&
                    settings.getJSONObject("search").getJSONObject("slowlog").containsKey("threshold") &&
                    settings.getJSONObject("search").getJSONObject("slowlog").getJSONObject("threshold").containsKey("fetch")) {
                slowFetchThreshold = settings.getJSONObject("search")
                        .getJSONObject("slowlog")
                        .getJSONObject("threshold")
                        .getJSONObject("fetch")
                        .getString("info");
            }

            String slowQueryThreshold = null;
            if (settings != null && settings.containsKey("search") &&
                    settings.getJSONObject("search").containsKey("slowlog") &&
                    settings.getJSONObject("search").getJSONObject("slowlog").containsKey("threshold") &&
                    settings.getJSONObject("search").getJSONObject("slowlog").getJSONObject("threshold").containsKey("query")) {
                slowQueryThreshold = settings.getJSONObject("search")
                        .getJSONObject("slowlog")
                        .getJSONObject("threshold")
                        .getJSONObject("query")
                        .getString("info");
            }

            TemplateDetailView vo = new TemplateDetailView();
            vo.setName(name);
            vo.setIndexPatterns(indexPatterns);
            vo.setPriority(priority);
            vo.setLifecycleName(lifecycleName);
            vo.setRolloverAlias(rolloverAlias);
            vo.setNumberOfShards(numberOfShards);
            vo.setNumberOfReplicas(numberOfReplicas);
            vo.setRefreshInterval(refreshInterval);
            vo.setCodec(codec);
            vo.setMode(mode);
            vo.setIgnoreMalformed(ignoreMalformed);
            vo.setSlowIndexThreshold(slowIndexThreshold);
            vo.setSlowFetchThreshold(slowFetchThreshold);
            vo.setSlowQueryThreshold(slowQueryThreshold);

            result.add(vo);
        }

        return result;
    }

    @Override
    public JSONObject getOneTemplateDetail(String templateName) throws IOException {
        try {
            String api = "/_template/" + templateName;
            String response = elasticClientService.executeGetApi(api);
            JSONObject jsonObject = JSONObject.parseObject(response);
            return jsonObject;
        } catch (Exception e) {
            log.error("获取模型异常,templateName:{}", templateName, e.getMessage());
            String api = "/_index_template/" + templateName;
            String response = elasticClientService.executeGetApi(api);
            JSONObject jsonObject = JSONObject.parseObject(response);
            return jsonObject;
        }
    }

    @Override
    public List<TemplateHistoryView> getTemplateHistoryViewCache() {
        List<TemplateHistoryView> templateHistoryView = cacheTemplate.getIfPresent(TEMPLATE_KEY);
        if (templateHistoryView == null) {
            templateHistoryView = getTemplateHistoryView();
            cacheTemplate.put(TEMPLATE_KEY, templateHistoryView);
        }
        return templateHistoryView;
    }

    @Override
    public List<TemplateHistoryView> getTemplateHistoryView() {
        try {
            // 获取模板基本信息
            List<TemplateDetailView> templateInfoAllList = new ArrayList<>();
            try {
                List<TemplateDetailView> templateInfoList = getTemplateDetailListByNewApi();
                if( templateInfoList != null && templateInfoList.size() > 0){
                    templateInfoAllList.addAll(templateInfoList);
                }
            }catch (Exception e){
                log.error("获取模型异常", e.getMessage());
            }
            try {
                List<TemplateDetailView> oldInfoList = getTemplateDetailListByOldApi();
                if( oldInfoList != null && oldInfoList.size() > 0){
                    templateInfoAllList.addAll(oldInfoList);
                }
            }catch (Exception e){
                log.error("获取模型异常", e);
            }



            List<TemplateHistoryView> result = new ArrayList<>();

            for (TemplateDetailView info : templateInfoAllList) {
                TemplateHistoryView view = new TemplateHistoryView();
                view.setName(info.getName());

                // indexPatterns 转换为字符串（逗号分隔）
                if (info.getIndexPatterns() != null && !info.getIndexPatterns().isEmpty()) {
                    view.setIndexPatterns(String.join(",", info.getIndexPatterns()));
                }

                // 设置 order 字段（假设 priority 对应 order）
                view.setOrder(info.getPriority());

                // 提取详细设置
                if (info.getContent() != null) {
                    try {
                        JSONObject detailJson = JSONObject.parseObject(info.getContent());
                        extractTemplateDetails(view, detailJson);
                    } catch (Exception e) {
                        log.warn("解析模板 {} 的 content JSON 失败", info.getName(), e);
                    }
                }

                result.add(view);
            }

            // 排序
            result = result.stream()
                    .sorted(Comparator.comparing(TemplateHistoryView::getName))
                    .collect(Collectors.toList());


            return result;
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return new ArrayList<>();
        }
    }

    private List<TemplateDetailView> getTemplateDetailListByOldApi() throws IOException {
        String api = "/_cat/templates?v=true&format=json";
        String response = elasticClientService.executeGetApi(api);

        JSONArray jsonArray = JSONArray.parseArray(response);
        List<TemplateDetailView> detailList = new ArrayList<>();

        for (Object obj : jsonArray) {
            JSONObject templateObj = (JSONObject) obj;
            String name = templateObj.getString("name");
            String patternsStr = templateObj.getString("index_patterns");
            String orderStr = templateObj.getString("order");

            TemplateDetailView detailView = new TemplateDetailView();
            detailView.setName(name);

            // 解析 index_patterns 字符串
            if (patternsStr != null && !patternsStr.isEmpty()) {
                try {
                    // 去掉两边的 [ 和 ]
                    patternsStr = patternsStr.trim().replaceAll("\\[", "");
                    patternsStr = patternsStr.trim().replaceAll("]", "");
                    // 分割成多个 pattern
                    String[] patternsArray = patternsStr.split(",");
                    List<String> patternList = new ArrayList<>();
                    for (String pattern : patternsArray) {
                        patternList.add(pattern.trim());
                    }
                    detailView.setIndexPatterns(patternList);
                } catch (Exception e) {
                    // 出错时记录日志或忽略
                    detailView.setIndexPatterns(Collections.singletonList(patternsStr.trim()));
                }
            }

            // 设置 priority（由 order 转换）
            if (orderStr != null && !orderStr.isEmpty()) {
                try {
                    detailView.setPriority(Integer.parseInt(orderStr));
                } catch (NumberFormatException ignored) {}
            }

            detailList.add(detailView);
        }

        return detailList;
    }

    /**
     * 根据索引名称获取其使用的模板信息（支持新旧模板）
     */
    public JSONObject getSettingByIndexName(String indexName) {
        if (StringUtils.isBlank(indexName)) {
            log.warn("索引名称为空，无法获取模板");
            return null;
        }

        try {
            // 第一步：获取索引的设置信息，从中提取模板名称
            String settingsApi = "/" + indexName + "/_settings";
            String settingsResponse = elasticClientService.executeGetApi(settingsApi);
            JSONObject settingsJson = JSONObject.parseObject(settingsResponse);

            // 检查是否包含模板信息
            JSONObject indexSettings = settingsJson.getJSONObject(indexName)
                    .getJSONObject("settings");
            return indexSettings;
        } catch (Exception e) {
            log.error("获取模板失败，索引名: {}", indexName, e);
            return null;
        }
    }

    /**
     * 删除 Elasticsearch 索引模板
     *
     * @param templateName 模板名称
     * @return 是否删除成功
     */
    @Override
    public boolean deleteTemplate(String templateName) throws IOException {
        if (StringUtils.isBlank(templateName)) {
            log.error("模板名称不能为空");
            return false;
        }

        // 获取当前集群信息
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterVersion = currentCluster.getClusterVersion();

        String apiUrl;
        if (ClusterVersionUtils.is7xVersion(clusterVersion)) {
            apiUrl = "/_template/" + templateName;
        } else if (ClusterVersionUtils.is8xVersion(clusterVersion)) {
            apiUrl = "/_index_template/" + templateName;
        } else {
            log.error("不支持的集群版本: {}", clusterVersion);
            return false;
        }

        // 执行删除请求（注意：DELETE 请求通常不需要传递 body）
        Response response = elasticClientService.executeDeleteApiReturnResponse(apiUrl, null);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200 || statusCode == 201) {
            log.info("模板 [{}] 删除成功，状态码：{}", templateName, statusCode);
            return true;
        } else {
            log.error("模板 [{}] 删除失败，状态码：{}", templateName, statusCode);
            return false;
        }
    }

    /**
     * 从模板详情中提取更多信息
     *
     * @param view           模板视图对象
     * @param templateDetail 模板详情JSON
     */
    private void extractTemplateDetails(TemplateHistoryView view, JSONObject templateDetail) {
        try {
            // 适用于新版本 Elasticsearch 的结构
            if (templateDetail.containsKey("index_templates")) {
                JSONObject indexTemplate = templateDetail.getJSONArray("index_templates").getJSONObject(0)
                        .getJSONObject("index_template");

                if (indexTemplate.containsKey("template") &&
                        indexTemplate.getJSONObject("template").containsKey("settings") &&
                        indexTemplate.getJSONObject("template").getJSONObject("settings").containsKey("index")) {

                    JSONObject settings = indexTemplate.getJSONObject("template")
                            .getJSONObject("settings").getJSONObject("index");

                    // 生命周期策略名称（滚动策略）
                    if (settings.containsKey("lifecycle")) {
                        JSONObject lifecycle = settings.getJSONObject("lifecycle");
                        if (lifecycle.containsKey("name")) {
                            view.setRollingPolicy(lifecycle.getString("name"));
                        }
                    }

                    // 保存策略（store_policy）
                    if (settings.containsKey("store")) {
                        JSONObject store = settings.getJSONObject("store");
                        if (store.containsKey("policy")) {
                            view.setStorePolicy(store.getString("policy"));
                        }
                    }

                    // 索引分片数
                    if (settings.containsKey("number_of_shards")) {
                        view.setIndexNum(settings.getString("number_of_shards"));
                    }

                    // 自动关闭天数
                    if (settings.containsKey("lifecycle_stop_after")) {
                        view.setCloseDays(settings.getString("lifecycle_stop_after"));
                    }

                }
            }
        } catch (Exception e) {
            log.warn("提取模板{}详细信息失败: {}", view.getName(), e.getMessage());
        }
    }

}
