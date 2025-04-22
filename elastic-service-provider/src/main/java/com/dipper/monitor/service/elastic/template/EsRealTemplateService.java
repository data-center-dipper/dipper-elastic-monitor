package com.dipper.monitor.service.elastic.template;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

public interface EsRealTemplateService {

    public boolean isExistTemplate(String name) throws IOException;

    boolean saveOrUpdateTemplate(String enName,JSONObject templateJson) throws IOException;
}
