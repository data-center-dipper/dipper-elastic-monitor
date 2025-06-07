package com.dipper.monitor.service.elastic.template;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.life.EsTemplateConfigMes;
import com.dipper.monitor.entity.elastic.template.history.EsTemplateInfo;
import com.dipper.monitor.entity.elastic.template.history.TemplateDetailView;
import com.dipper.monitor.entity.elastic.template.history.TemplateHistoryView;

import java.io.IOException;
import java.util.List;

public interface ElasticRealTemplateService {

    public boolean isExistTemplate(String name) throws IOException;

    boolean saveOrUpdateTemplate(String enName,JSONObject templateJson) throws IOException;

   List<EsTemplateConfigMes> statTemplate(String name) throws IOException;

   
    List<String> getIndexPatternList(String indexPatterns);

    /**
     * 滚动索引
     * @param id
     * @return
     */
    JSONObject rollTemplate(Integer id) throws Exception;

    /**
     * 获取模版的简单信息，只有个别信息
     * @return
     * @throws IOException
     */
    List<EsTemplateInfo> getTemplateList() throws IOException;

    /**
     * 获取模版，包含详情信息，一些重要信息
     * @return
     * @throws IOException
     */
    List<TemplateDetailView> getTemplateDetailListByNewApi() throws IOException;

    /**
     * 获取单个模版的详情，最原始的详情信息
     * @return
     * @throws IOException
     */
    JSONObject getOneTemplateDetail(String templateName) throws IOException;

    /**
     * 获取模版缓存信息
     * @return
     */
    List<TemplateHistoryView> getTemplateHistoryViewCache();

    List<TemplateHistoryView> getTemplateHistoryView();

    JSONObject getSettingByIndexName(String indexName);

   /**
    * 删除模版
    * @param templateName
    */
   boolean deleteTemplate(String templateName) throws IOException;
}
