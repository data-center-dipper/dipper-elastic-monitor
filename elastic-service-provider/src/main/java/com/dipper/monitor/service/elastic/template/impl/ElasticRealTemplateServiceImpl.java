package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.dipper.client.proxy.params.elasticsearch.Response;
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
import com.dipper.monitor.utils.CommonThreadFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.nio.entity.NStringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 *    // 可选接口 GET /_cat/templates?format=json
 *         // 获取模版详情 GET /_index_template/ailpha-logs-20250517
 *         // 获取模版，包含详情信息  GET /_index_template
 */
@Slf4j
@Service
public class ElasticRealTemplateServiceImpl implements ElasticRealTemplateService {

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
                .expireAfterWrite(300L, TimeUnit.SECONDS)
                .expireAfterAccess(300L, TimeUnit.SECONDS)
                .concurrencyLevel(7)
                .build();
    }


    public boolean isExistTemplate(String name) throws IOException {
        String api = "/_template/" + name;
        boolean b = elasticClientService.executeHeadApi(api);
        return b;
    }

    public boolean saveOrUpdateTemplate(String name,JSONObject templateJson) {
        try {
            String method = isExistTemplate(name) ? "POST" : "PUT";
            NStringEntity nStringEntity = new NStringEntity(templateJson.toJSONString());
            Response response = null;
            if("POST".equalsIgnoreCase(method)){
                response = elasticClientService.executePostApiReturnResponse("/_index_template/" + name, nStringEntity);
            }else {
                response = elasticClientService.executePutApiReturnResponse("/_index_template/" + name, nStringEntity);
            }
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
            log.error("索引模板创建失败：{}", response);
            return false;
        } catch (Exception e) {
            log.error("索引模板创建失败：{}",templateJson, e);
            return false;
        }
    }

    /**
     * 点开某个模版 查看模版的详情
     * @param name
     * @return
     * @throws IOException
     */
    @Override
    public List<EsTemplateConfigMes> statTemplate(String name) throws IOException {
        StatTemplateHandler statTemplateHandler = new StatTemplateHandler(elasticClientService,
                elasticRealIndexService,elasticShardService,
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
        if(oneUnconvertedTemplate == null){
            throw new RuntimeException("模板不存在");
        }

        RollingIndexByTemplateHandler rollingIndexByTemplateHandler = new RollingIndexByTemplateHandler(elasticHealthService,
                elasticStoreTemplateService,templatePreviewService);
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
                new TypeReference<List<EsTemplateInfo>>() {}
        );

        return templateInfoList;
    }

    // 获取模版，包含详情信息  GET /_index_template
    @Override
    public List<TemplateDetailView> getTemplateDetailList() throws IOException {
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
            String api = "/_template/"+templateName;
            String response = elasticClientService.executeGetApi(api);
            JSONObject jsonObject = JSONObject.parseObject(response);
            return jsonObject;
        }catch (Exception e){
            log.error("获取模型异常,templateName:{}",templateName,e.getMessage());
            String api = "/_index_template/"+templateName;
            String response = elasticClientService.executeGetApi(api);
            JSONObject jsonObject = JSONObject.parseObject(response);
            return jsonObject;
        }
    }

    @Override
    public List<TemplateHistoryView> getTemplateHistoryViewCache() {
        List<TemplateHistoryView> templateHistoryView = cacheTemplate.getIfPresent(TEMPLATE_KEY);
        if(templateHistoryView == null){
            templateHistoryView = getTemplateHistoryView();
            cacheTemplate.put(TEMPLATE_KEY,templateHistoryView);
        }
        return templateHistoryView;
    }

    @Override
    public List<TemplateHistoryView> getTemplateHistoryView() {
        try {
            // 获取模板基本信息
            List<TemplateDetailView> templateInfoList = getTemplateDetailList();

            List<TemplateHistoryView> result = new ArrayList<>();

            for (TemplateDetailView info : templateInfoList) {
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

            return result;
        } catch (IOException e) {
            log.error("获取模板列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 从模板详情中提取更多信息
     *
     * @param view 模板视图对象
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
