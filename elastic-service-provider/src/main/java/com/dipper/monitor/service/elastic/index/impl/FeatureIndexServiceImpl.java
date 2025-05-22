package com.dipper.monitor.service.elastic.index.impl;

import com.dipper.monitor.controller.elastic.template.TemplateStoreController;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.db.elastic.IndexWriteEntity;
import com.dipper.monitor.service.elastic.index.FeatureIndexService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class FeatureIndexServiceImpl implements FeatureIndexService {

    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;

    @Override
    public List<IndexWriteEntity> featureIndexList() {
        List<EsTemplateEntity> allTemplates = elasticStoreTemplateService.getAllTemplates();
        for (EsTemplateEntity item:allTemplates){
            getFeatureIndexList(item);
        }
        return List.of();
    }

    @Override
    public List<IndexWriteEntity> getFeatureIndexList(EsTemplateEntity entity) {
        return List.of();
    }
}
