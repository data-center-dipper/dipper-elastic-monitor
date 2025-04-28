package com.dipper.monitor.service.elastic.template;


import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.life.EsTemplateStatEntity;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;

import java.util.List;

public interface ElasticStoreTemplateService {
    /**
     * 仅仅 添加模版信息
     * @param esUnconvertedTemplate
     * @return
     */
    EsTemplateEntity addOrUpdateTemplate(EsUnconvertedTemplate esUnconvertedTemplate);
    EsTemplateEntity getTemplate(Long id);
    EsTemplateEntity updateTemplate(EsTemplateEntity esTemplateEntity);
    void deleteTemplate(Long id);
    List<EsTemplateEntity> getAllTemplates();

    /**
     * 添加模版信息，并实时生效,滚动索引
     *
     * @param esUnconvertedTemplate
     * @return
     */
    void addAndRollTemplate(EsUnconvertedTemplate esUnconvertedTemplate) throws Exception;

    /**
     * 滚动模版
     * @param esUnconvertedTemplate
     */
    void rollTemplate(EsUnconvertedTemplate esUnconvertedTemplate) throws Exception;

    void updateTemplateStat(List<EsTemplateStatEntity> templateStat);
}