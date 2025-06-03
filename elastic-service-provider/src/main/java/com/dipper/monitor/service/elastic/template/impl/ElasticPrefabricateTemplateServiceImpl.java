package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.config.template.TemplateConfig;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.entity.elastic.template.unconverted.PrefabricateTemplateEntity;
import com.dipper.monitor.entity.elastic.template.unconverted.PrefabricateTemplateNames;
import com.dipper.monitor.service.elastic.template.ElasticPrefabricateTemplateService;
import com.dipper.monitor.service.elastic.template.impl.handlers.prefabricate.PrefabricateTemplateInitHandler;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 预制模板，模板的模板
 */
@Service
@Slf4j
public class ElasticPrefabricateTemplateServiceImpl implements ElasticPrefabricateTemplateService {

    @Autowired
    private TemplateConfig templateConfig;

    private PrefabricateTemplateInitHandler  prefabricateTemplateInitHandler;

    @PostConstruct
    public void init() {
        log.info("准备初始化模板的模板");
        try {
            CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
            String clusterVersion = currentCluster.getClusterVersion();

            prefabricateTemplateInitHandler = new PrefabricateTemplateInitHandler(templateConfig,clusterVersion);
            prefabricateTemplateInitHandler.initTemplate();
        }catch (Exception e){
            log.error("初始化模板的模板失败",e);
        }
    }


    @Override
    public List<EsUnconvertedTemplate> prefabricateTemplate() {
        List<PrefabricateTemplateEntity> prefabricateTemplateList = prefabricateTemplateInitHandler.getEsUnconvertedTemplateList();
        // 转成成 EsUnconvertedTemplate
        if(prefabricateTemplateList == null || prefabricateTemplateList.size() == 0){
            return Collections.emptyList();
        }
        List<EsUnconvertedTemplate> esUnconvertedTemplateList = new ArrayList<>();
        for (PrefabricateTemplateEntity entity : prefabricateTemplateList) {
            EsUnconvertedTemplate esUnconvertedTemplate = new EsUnconvertedTemplate();
            BeanUtils.copyProperties(entity,esUnconvertedTemplate);
            esUnconvertedTemplate.setTemplateContent(JSONObject.toJSONString(entity.getTemplateContent()));
            esUnconvertedTemplateList.add(esUnconvertedTemplate);
        }
        return esUnconvertedTemplateList;
    }

    @Override
    public List<PrefabricateTemplateNames> prefabricateTemplateNames() {
        List<PrefabricateTemplateEntity> prefabricateTemplateList = prefabricateTemplateInitHandler.getEsUnconvertedTemplateList();
        // 转成成 EsUnconvertedTemplate
        if(prefabricateTemplateList == null || prefabricateTemplateList.size() == 0){
            return Collections.emptyList();
        }
        List<PrefabricateTemplateNames> esUnconvertedTemplateList = new ArrayList<>();
        for (PrefabricateTemplateEntity entity : prefabricateTemplateList) {
            PrefabricateTemplateNames prefabricateTemplateNames = new PrefabricateTemplateNames();
            BeanUtils.copyProperties(entity,prefabricateTemplateNames);
            esUnconvertedTemplateList.add(prefabricateTemplateNames);
        }
        return esUnconvertedTemplateList;
    }

    @Override
    public EsUnconvertedTemplate prefabricateOneTemplate(String enNameWeb) {
        if(StringUtils.isBlank(enNameWeb)){
            throw new IllegalArgumentException("模板名称为空");
        }
        List<PrefabricateTemplateEntity> prefabricateTemplateList = prefabricateTemplateInitHandler.getEsUnconvertedTemplateList();
        // 转成成 EsUnconvertedTemplate
        if(prefabricateTemplateList == null || prefabricateTemplateList.size() == 0){
           throw new IllegalArgumentException("模板为空");
        }
        List<EsUnconvertedTemplate> esUnconvertedTemplateList = new ArrayList<>();
        for (PrefabricateTemplateEntity entity : prefabricateTemplateList) {
            String enName = entity.getEnName();
            if(enNameWeb.equalsIgnoreCase(enName)){
                EsUnconvertedTemplate esUnconvertedTemplate = new EsUnconvertedTemplate();
                BeanUtils.copyProperties(entity,esUnconvertedTemplate);
                esUnconvertedTemplate.setTemplateContent(JSONObject.toJSONString(entity.getTemplateContent()));
                return esUnconvertedTemplate;
            }
        }
        throw new IllegalArgumentException("模板为空");
    }
}
