package com.dipper.monitor.service.elastic.template.impl.handlers.preview;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class Preview8xTemplateHandler extends AbstractPreviewHandler {

    @Override
    protected void addOrUpdateMappings(JSONObject templateJson, String dicName) {
        JSONObject elasticMapping = elasticDicService.getElasticMapping(dicName);
        if (elasticMapping == null) {
            return;
        }

        // 获取template对象，如果不存在则创建
        JSONObject template = templateJson.getJSONObject("template");
        if (template == null) {
            template = new JSONObject();
            templateJson.put("template", template);
        }

        // 获取mappings对象，如果不存在则创建
        JSONObject mappings = template.getJSONObject("mappings");
        JSONObject allreadFields = getAllreadFields(mappings);
        if (mappings == null) {
            JSONObject jsonObject = new JSONObject().fluentPut("properties", elasticMapping);
            template.put("mappings", jsonObject);
            return;
        }
        
        // 模版中配置的字段优先级更高
        elasticMapping.putAll(allreadFields);
        JSONObject propertiesObj = mappings.getJSONObject("properties");
        if(propertiesObj == null){
            mappings.put("properties", elasticMapping);
        }else {
            mappings.put("properties", elasticMapping);
        }

        template.put("mappings", mappings);
    }
    
    @Override
    public JSONObject previewTemplate(EsUnconvertedTemplate esUnconvertedTemplate) {
        // 创建一个新的JSON对象来构建最终的模板
        String templateContent = esUnconvertedTemplate.getTemplateContent();
        JSONObject templateJson = JSONObject.parseObject(templateContent);
        JSONObject settingsWebManyValue = templateJson.getJSONObject("settings");
        if(settingsWebManyValue != null){
            // 兼容 7.x 版本的模版 要转成 8.x 版本的模版
            JSONObject settingsWebMany = new JSONObject();
            settingsWebMany.put("settings",settingsWebManyValue);
            templateJson.put("template",settingsWebMany);
            templateJson.remove("settings");
        }


        // 确保有template对象
        JSONObject template = templateJson.getJSONObject("template");
        if (template == null) {
            template = new JSONObject();
            templateJson.put("template", template);
        }
        
        // 添加settings部分到template对象
        Map<String, Object> settingsWeb = esUnconvertedTemplate.getSettings();
        if (settingsWeb != null && !settingsWeb.isEmpty()) {
            JSONObject settingsJson = template.getJSONObject("settings");
            if (settingsJson == null) {
                settingsJson = new JSONObject();
                template.put("settings", settingsJson);
            }
            
            for (Map.Entry<String, Object> entry : settingsWeb.entrySet()) {
                settingsJson.put(entry.getKey(), entry.getValue());
            }
        }
        
        // 添加mappings部分到template对象
        String dicName = esUnconvertedTemplate.getDicName();
        addOrUpdateMappings(templateJson, dicName);
        
        // 处理索引模式，确保是数组形式
        if (esUnconvertedTemplate.getIndexPatterns() != null && !esUnconvertedTemplate.getIndexPatterns().isEmpty()) {
            Object patterns = esUnconvertedTemplate.getIndexPatterns();
            if (patterns instanceof String) {
                // 如果是字符串，转换为数组
                JSONArray patternsArray = new JSONArray();
                patternsArray.add(patterns);
                templateJson.put("index_patterns", patternsArray);
            } else {
                templateJson.put("index_patterns", patterns);
            }
        }
        
        // 处理别名到template对象
        String aliansPatterns = esUnconvertedTemplate.getAliansPatterns();
        if (StringUtils.isNotBlank(aliansPatterns)) {
            JSONObject aliasesJson = new JSONObject();
            JSONObject aliasDetailsJson = new JSONObject();
            aliasDetailsJson.put("is_write_index", true);
            aliasesJson.put(aliansPatterns, aliasDetailsJson);
            template.put("aliases", aliasesJson);
        }
        
        // 处理 index 设置到template对象
        Integer numberOfShards = esUnconvertedTemplate.getNumberOfShards();
        Integer numberOfReplicas = esUnconvertedTemplate.getNumberOfReplicas();
        
        JSONObject settings = template.getJSONObject("settings");
        if(settings == null){
            settings = new JSONObject();
            template.put("settings", settings);
        }
        JSONObject index = settings.getJSONObject("index");
        if(index == null){
            index = new JSONObject();
            settings.put("index", index);
        }
        if (numberOfShards != null) {
            index.put("number_of_shards", numberOfShards);
        }
        if (numberOfReplicas != null) {
            index.put("number_of_replicas", numberOfReplicas);
        }
        
        // 添加优先级
        templateJson.put("priority", 50);
        
        return templateJson;
    }
}