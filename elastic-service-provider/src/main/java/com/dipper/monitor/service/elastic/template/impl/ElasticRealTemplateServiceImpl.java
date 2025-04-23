package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.nio.entity.NStringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ElasticRealTemplateServiceImpl implements ElasticRealTemplateService {

    @Autowired
    private ElasticClientService elasticClientService;

    public boolean isExistTemplate(String name) throws IOException {
        String api = "/_template/" + name;
        boolean b = elasticClientService.executeHeadApi(api);
        return b;
    }

    public boolean saveOrUpdateTemplate(String name,JSONObject templateJson) {
        try {
            String method = isExistTemplate(name) ? "POST" : "PUT";
            NStringEntity nStringEntity = new NStringEntity(templateJson.toJSONString());
            Response response = null;
            if("POST".equalsIgnoreCase(method)){
                response = elasticClientService.executePostApiReturnResponse("/_template/" + name, nStringEntity);
            }else {
                response = elasticClientService.executePutApiReturnResponse("/_template/" + name, nStringEntity);
            }
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
            log.error("索引模板创建失败：{}", response);
            return false;
        } catch (IOException e) {
            log.error("索引模板创建失败：{}", e);
            return false;
        }
    }

}
