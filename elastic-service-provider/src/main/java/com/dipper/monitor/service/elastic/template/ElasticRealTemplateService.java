package com.dipper.monitor.service.elastic.template;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.life.EsTemplateConfigMes;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;

import java.io.IOException;
import java.util.List;

public interface ElasticRealTemplateService {

    public boolean isExistTemplate(String name) throws IOException;

    boolean saveOrUpdateTemplate(String enName,JSONObject templateJson) throws IOException;

   List<EsTemplateConfigMes> statTemplate(String name) throws IOException;


    EsTemplateEntity getTemplate(Integer id);

    List<String> getIndexPatternList(String indexPatterns);

    /**
     * 滚动索引
     * @param id
     * @return
     */
    JSONObject rollTemplate(Integer id) throws Exception;
}
