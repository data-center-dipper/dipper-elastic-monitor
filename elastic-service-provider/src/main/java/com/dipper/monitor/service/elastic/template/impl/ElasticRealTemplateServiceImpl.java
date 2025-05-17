package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.life.EsTemplateConfigMes;
import com.dipper.monitor.entity.elastic.template.history.EsTemplateInfo;
import com.dipper.monitor.entity.elastic.template.history.TemplateDetailView;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.http.nio.entity.NStringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        List<TemplateDetailView> result = new ArrayList<>();

        for (int i = 0; i < templatesArray.size(); i++) {
            JSONObject templateObj = templatesArray.getJSONObject(i);
            String name = templateObj.getString("name");

            JSONObject indexTemplate = templateObj.getJSONObject("index_template");
            JSONArray indexPatterns = indexTemplate.getJSONArray("index_patterns");
            Integer priority = indexTemplate.getInteger("priority");

            JSONObject template = indexTemplate.getJSONObject("template");
            JSONObject settings = template.getJSONObject("settings").getJSONObject("index");

            String lifecycleName = settings.getJSONObject("lifecycle").getString("name");
            String rolloverAlias = settings.getJSONObject("lifecycle").getString("rollover_alias");
            String numberOfShards = settings.getString("number_of_shards");
            String numberOfReplicas = settings.getString("number_of_replicas");
            String refreshInterval = settings.getString("refresh_interval");
            String codec = settings.getString("codec");
            String mode = settings.getString("mode");
            String ignoreMalformed = settings.getJSONObject("mapping").getString("ignore_malformed");

            String slowIndexThreshold = settings.getJSONObject("indexing")
                    .getJSONObject("slowlog")
                    .getJSONObject("threshold")
                    .getJSONObject("index")
                    .getString("info");

            String slowFetchThreshold = settings.getJSONObject("search")
                    .getJSONObject("slowlog")
                    .getJSONObject("threshold")
                    .getJSONObject("fetch")
                    .getString("info");

            String slowQueryThreshold = settings.getJSONObject("search")
                    .getJSONObject("slowlog")
                    .getJSONObject("threshold")
                    .getJSONObject("query")
                    .getString("info");

            TemplateDetailView vo = new TemplateDetailView();
            vo.setName(name);
            vo.setIndexPatterns(indexPatterns.toJavaList(String.class));
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
        String api = "/_index_template/"+templateName;
        String response = elasticClientService.executeGetApi(api);
        JSONObject jsonObject = JSONObject.parseObject(response);
        return jsonObject;
    }


}
