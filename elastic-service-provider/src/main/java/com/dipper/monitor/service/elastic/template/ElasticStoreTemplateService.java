package com.dipper.monitor.service.elastic.template;


import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.life.EsTemplateStatEntity;
import com.dipper.monitor.entity.elastic.template.AutoCreateReq;
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

    EsTemplateEntity getTemplate(Integer id);
    /**
     * 更新模板信息
     * @param esUnconvertedTemplate 未转换的模板信息
     * @return 更新后的模板实体
     */
    EsTemplateEntity updateTemplate(EsUnconvertedTemplate esUnconvertedTemplate);
    void deleteTemplate(Long id);
    List<EsTemplateEntity> getAllTemplates();


    void updateTemplateStat(List<EsTemplateStatEntity> templateStat);

    /**
     * 根据英文名称查询模版，并且统计模版的信息
     * @param id
     * @return
     */
    ElasticTemplateView getTemplateAndStat(Integer id);

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
    EsUnconvertedTemplate getOneUnconvertedTemplate(Integer id);

    EsUnconvertedTemplate getOneUnconvertedTemplateByEnName(String enName);

    /**
     * 获取模版统计信息
     * @param id
     * @return
     */
    EsTemplateStatEntity templateStat(Integer id);

    /**
     * 根据模版名称查询模版
     * @param templateLike
     * @return
     */
    List<String> templateNames(String templateLike);


    boolean batchInsertTemplates(List<EsTemplateEntity> toBeSaved);

    /**
     * 更新模板自动创建设置
     * @param autoCreateReq
     */
    void updateAutoCreate(AutoCreateReq autoCreateReq);
}