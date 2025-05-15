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
public abstract class AbstractPreviewHandler {

    protected DicService dicService;
    protected WordService wordService;
    protected ElasticDicService elasticDicService;

    public AbstractPreviewHandler() {
        dicService = SpringUtil.getBean(DicService.class);
        wordService = SpringUtil.getBean(WordService.class);
        elasticDicService = SpringUtil.getBean(ElasticDicService.class);
    }

    /**
     * 预览模板
     * @param esUnconvertedTemplate 未转换的ES模板
     * @return 预览后的模板JSON对象
     */
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

            // 处理别名
            appendAliases(templateJson, esUnconvertedTemplate);

            // 处理 index 设置
            updateIndexSettings(templateJson, esUnconvertedTemplate);

            return templateJson;
        } catch (Exception e) {
            log.error("Error generating preview template", e);
            throw new RuntimeException("Error generating preview template", e);
        }
    }

    /**
     * 更新索引设置
     */
    protected void updateIndexSettings(JSONObject templateJson, EsUnconvertedTemplate esUnconvertedTemplate) {
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
            settings.put("index", index);
        }
        if (numberOfShards != null) {
            index.put("number_of_shards", numberOfShards);
        }
        if (numberOfReplicas != null) {
            index.put("number_of_replicas", numberOfReplicas);
        }
    }

    /**
     * 添加别名
     */
    protected void appendAliases(JSONObject templateJson, EsUnconvertedTemplate esUnconvertedTemplate) {
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

    /**
     * 获取已有字段
     */
    protected JSONObject getAllreadFields(JSONObject mappings) {
        if (mappings == null) {
            return new JSONObject();
        }
        JSONObject docObj = mappings.getJSONObject("_doc");
        if (docObj == null) {
            return new JSONObject();
        }
        JSONObject properties = docObj.getJSONObject("properties");
        return properties != null ? properties : new JSONObject();
    }

    /**
     * 添加或更新设置
     */
    protected void addOrUpdateSetting(JSONObject templateJson, Map<String, Object> settings) {
        if (settings == null || settings.isEmpty()) {
            return;
        }
        JSONObject settingsJson = templateJson.getJSONObject("settings");
        if (settingsJson == null) {
            settingsJson = new JSONObject();
            templateJson.put("settings", settingsJson);
        }
        JSONObject index = settingsJson.getJSONObject("index");
        if (index == null) {
            index = new JSONObject();
            settingsJson.put("index", index);
        }
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            Object value = entry.getValue();
            if (StringUtils.isBlank(String.valueOf(value))) {
                continue;
            }
            index.put(entry.getKey(), entry.getValue());
        }
        templateJson.put("settings", settingsJson);
    }

    /**
     * 添加或更新映射
     * 由子类实现不同版本的映射添加逻辑
     */
    protected abstract void addOrUpdateMappings(JSONObject templateJson, String dicName);
}
