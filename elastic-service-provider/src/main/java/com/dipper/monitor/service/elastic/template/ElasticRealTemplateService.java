package com.dipper.monitor.service.elastic.template;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.life.EsTemplateConfigMes;

import java.io.IOException;
import java.util.List;

public interface ElasticRealTemplateService {

    public boolean isExistTemplate(String name) throws IOException;

    boolean saveOrUpdateTemplate(String enName,JSONObject templateJson) throws IOException;

   List<EsTemplateConfigMes> statTemplate(String name) throws IOException;
}
