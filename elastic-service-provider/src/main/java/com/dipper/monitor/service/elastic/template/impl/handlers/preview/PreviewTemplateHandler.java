package com.dipper.monitor.service.elastic.template.impl.handlers.preview;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.dic.DicService;
import com.dipper.monitor.service.elastic.dic.ElasticDicService;
import com.dipper.monitor.service.elastic.dic.WordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


import java.util.Map;

@Slf4j
public class PreviewTemplateHandler {

    private DicService dicService;
    private WordService wordService;
    private ElasticDicService elasticDicService;

    public PreviewTemplateHandler() {
        dicService = SpringUtil.getBean(DicService.class);
        wordService = SpringUtil.getBean(WordService.class);
        elasticDicService = SpringUtil.getBean(ElasticDicService.class);
    }

    public JSONObject previewTemplate(EsUnconvertedTemplate esUnconvertedTemplate) {
        try {
            // 创建一个新的JSON对象来构建最终的模板
            String templateContent = esUnconvertedTemplate.getTemplateContent();
            JSONObject templateJson = JSONObject.parseObject(templateContent);


            // 添加settings部分
            Map<String, Object> settings = esUnconvertedTemplate.getSettings();
            addOrUpdateSetting(templateJson, settings);

            // 添加mappings部分
            String dicName = esUnconvertedTemplate.getDicName();
            addOrUpdateMappings(templateJson, dicName);

            // 添加索引模式
            if (esUnconvertedTemplate.getIndexPatterns() != null && !esUnconvertedTemplate.getIndexPatterns().isEmpty()) {
                templateJson.put("index_patterns", esUnconvertedTemplate.getIndexPatterns());
            }

            // 处理别名（这里假设为单个别名，实际情况可能不同）
            appendAliases(templateJson, esUnconvertedTemplate);

            // 处理 index 设置
            updateIndexSettings(templateJson, esUnconvertedTemplate);


            return templateJson;
        } catch (Exception e) {
            log.error("Error generating preview template", e);
            throw new RuntimeException("Error generating preview template", e);
        }
    }

    private void updateIndexSettings(JSONObject templateJson, EsUnconvertedTemplate esUnconvertedTemplate) {
        // 将其他必要字段添加到settings中
        Integer numberOfShards = esUnconvertedTemplate.getNumberOfShards();
        Integer numberOfReplicas = esUnconvertedTemplate.getNumberOfReplicas();

        JSONObject settings = templateJson.getJSONObject("settings");
        if(settings == null){
            settings = new JSONObject();
            templateJson.put("settings", settings);
        }
        JSONObject index = settings.getJSONObject("index");
        if(index == null){
            index = new JSONObject();
            templateJson.put("settings", settings);
        }
        if (numberOfShards != null) {
            index.put("number_of_shards", numberOfShards);
        }
        if (numberOfReplicas != null) {
            index.put("number_of_replicas", numberOfReplicas);
        }
    }

    private void appendAliases(JSONObject templateJson, EsUnconvertedTemplate esUnconvertedTemplate) {
        String aliansPatterns = esUnconvertedTemplate.getAliansPatterns();
        if (StringUtils.isBlank(aliansPatterns)) {
            throw new RuntimeException("Aliases patterns is blank");
        }
        JSONObject aliasesJson = new JSONObject();

        JSONObject aliasDetailsJson = new JSONObject();
        aliasDetailsJson.put("is_write_index", true); // 示例默认值
        aliasesJson.put(aliansPatterns, aliasDetailsJson);
        templateJson.put("aliases", aliasesJson);
    }

    private void addOrUpdateMappings(JSONObject templateJson, String dicName) {

        JSONObject elasticMapping = elasticDicService.getElasticMapping(dicName);
        if (elasticMapping == null) {
            return;
        }

        JSONObject mappings = templateJson.getJSONObject("mappings");
        JSONObject allreadFields = getAllreadFields(mappings);
        if (mappings == null) {
            mappings = new JSONObject();
            mappings.put("_doc", new JSONObject().fluentPut("properties", elasticMapping));
            templateJson.put("mappings", mappings);
            return;
        }
        // todo: 模版中配置的字段优先级更高
        elasticMapping.putAll(allreadFields);
        JSONObject docObj = mappings.getJSONObject("_doc");
        if(docObj == null){
            mappings.put("properties", elasticMapping);
        }else {
            docObj.put("properties", elasticMapping);
        }

        templateJson.put("mappings", mappings);
    }

    private JSONObject getAllreadFields(JSONObject mappings) {
        if (mappings == null) {
            return new JSONObject();
        }
        JSONObject docObj = mappings.getJSONObject("_doc");
        if (docObj == null) {
            return new JSONObject();
        }
        JSONObject properties = docObj.getJSONObject("properties");
        return properties;
    }

    private void addOrUpdateSetting(JSONObject templateJson, Map<String, Object> settings) {
        if (settings == null || settings.isEmpty()) {
            return;
        }
        JSONObject settingsJson = templateJson.getJSONObject("settings");
        JSONObject index = settingsJson.getJSONObject("index");
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            Object value = entry.getValue();
            if (StringUtils.isBlank(String.valueOf(value))) {
                continue;
            }
            index.put(entry.getKey(), entry.getValue());
        }
        templateJson.put("settings", settingsJson);
    }
}