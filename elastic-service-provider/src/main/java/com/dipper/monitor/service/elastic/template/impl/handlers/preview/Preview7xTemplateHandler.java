package com.dipper.monitor.service.elastic.template.impl.handlers.preview;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Preview7xTemplateHandler extends AbstractPreviewHandler {

    @Override
    protected void addOrUpdateMappings(JSONObject templateJson, String dicName) {
        JSONObject elasticMapping = elasticDicService.getElasticMapping(dicName);
        if (elasticMapping == null) {
            return;
        }

        JSONObject mappings = templateJson.getJSONObject("mappings");
        JSONObject allreadFields = getAllreadFields(mappings);
        if (mappings == null) {
            mappings =  new JSONObject().fluentPut("properties", elasticMapping);
            templateJson.put("mappings", mappings);
            return;
        }
        // 模版中配置的字段优先级更高
        elasticMapping.putAll(allreadFields);
        JSONObject docObj = mappings.getJSONObject("_doc");
        if(docObj == null){
            mappings.put("properties", elasticMapping);
        }else {
            docObj.put("properties", elasticMapping);
        }

        templateJson.put("mappings", mappings);
    }
    
    @Override
    public JSONObject previewTemplate(EsUnconvertedTemplate esUnconvertedTemplate) {
        JSONObject templateJson = super.previewTemplate(esUnconvertedTemplate);
        return templateJson;
    }
}