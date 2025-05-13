package com.dipper.monitor.service.elastic.template;


import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.life.EsTemplateStatEntity;
import com.dipper.monitor.entity.elastic.template.ElasticTemplateListView;
import com.dipper.monitor.entity.elastic.template.ElasticTemplateView;
import com.dipper.monitor.entity.elastic.template.TemplatePageInfo;
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
    /**
     * 更新模板信息
     * @param esUnconvertedTemplate 未转换的模板信息
     * @return 更新后的模板实体
     */
    EsTemplateEntity updateTemplate(EsUnconvertedTemplate esUnconvertedTemplate);
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

    /**
     * 根据英文名称查询模版，并且统计模版的信息
     * @param id
     * @return
     */
    ElasticTemplateView getTemplateAndStat(Long id);

    /**
     * 根据条件查询模版
     * @param templatePageInfo
     * @return
     */
    Integer getTemplateNum(TemplatePageInfo templatePageInfo);

    /**
     * 根据条件查询模版
     * @param templatePageInfo
     * @return
     */
    List<EsTemplateEntity> getTemplateByPage(TemplatePageInfo templatePageInfo);

    /**
     * 根据条件查询模版
     * @param templatePageInfo
     * @return
     */
    List<ElasticTemplateListView> getTemplateListViewByPage(TemplatePageInfo templatePageInfo);

    /**
     * 获取未经转换的模版详情
     * @param id
     * @return
     */
    EsUnconvertedTemplate getOneUnconvertedTemplate(Long id);

    /**
     * 获取模版统计信息
     * @param id
     * @return
     */
    EsTemplateStatEntity templateStat(Long id);
}