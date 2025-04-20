package com.dipper.monitor.service.elastic.template;


import com.dipper.monitor.entity.elastic.template.EsTemplate;

import java.util.List;

public interface EsTemplateService {
    EsTemplate addTemplate(EsTemplate esTemplate);
    EsTemplate getTemplate(Long id);
    EsTemplate updateTemplate(EsTemplate esTemplate);
    void deleteTemplate(Long id);
    List<EsTemplate> getAllTemplates();
}