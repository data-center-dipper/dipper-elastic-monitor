package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.life.EsTemplateConfigMes;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticIndexService;
import com.dipper.monitor.service.elastic.segment.ElasticSegmentService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import com.dipper.monitor.service.elastic.template.impl.handlers.StatTemplateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.nio.entity.NStringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ElasticRealTemplateServiceImpl implements ElasticRealTemplateService {

    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticIndexService elasticIndexService;
    @Autowired
    private ElasticShardService elasticShardService;
    @Autowired
    private ElasticSegmentService elasticSegmentService;

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

    /**
     * 点开某个模版 查看模版的详情
     * @param name
     * @return
     * @throws IOException
     */
    @Override
    public List<EsTemplateConfigMes> statTemplate(String name) throws IOException {
        StatTemplateHandler statTemplateHandler = new StatTemplateHandler(elasticClientService,
                elasticIndexService,elasticShardService,
                elasticSegmentService);
        return statTemplateHandler.statTemplate(name);
    }

}
