package com.dipper.monitor.service.elastic.setting.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.index.IndexSettingReq;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.setting.IndexSettingService;
import com.dipper.monitor.service.elastic.setting.index.UpdateIndexSettingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IndexSettingServiceImpl implements IndexSettingService {

    @Autowired
    private ElasticRealIndexService elasticRealIndexService;
    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticRealNodeService elasticRealNodeService;

    @Override
    public JSONObject getIndexSetting(String indexName) {
        return elasticRealIndexService.getIndexSetting(indexName);
    }

    @Override
    public JSONObject getMappingByIndexName(String indexName) {
        return elasticRealIndexService.getMappingByIndexName(indexName);
    }

    @Override
    public void updateIndexSetting(IndexSettingReq indexSettingReqn) {
        UpdateIndexSettingHandler updateIndexSettingHandler = new UpdateIndexSettingHandler(indexSettingReqn,
                elasticRealIndexService,elasticClientService,elasticRealNodeService);
        updateIndexSettingHandler.handle();
    }
}
