package com.dipper.monitor.service.elastic.template;


import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.EsTemplate;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;

import java.util.List;

public interface EsTemplateService {
    EsTemplate addTemplate(EsTemplate esTemplate);
    EsTemplate getTemplate(Long id);
    EsTemplate updateTemplate(EsTemplate esTemplate);
    void deleteTemplate(Long id);
    List<EsTemplate> getAllTemplates();

    /**
     * 预览生成的模版
     * @param esUnconvertedTemplate
     * @return
     */
    JSONObject previewTemplate(EsUnconvertedTemplate esUnconvertedTemplate);
}