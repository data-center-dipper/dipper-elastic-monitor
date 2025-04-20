package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.template.EsTemplate;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.mapper.EsTemplateMapper;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.template.EsTemplateService;
import com.dipper.monitor.service.elastic.template.impl.handlers.PreviewTemplateHandler;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EsTemplateServiceImpl implements EsTemplateService {

    @Autowired
    private EsTemplateMapper esTemplateMapper;

    @Override
    public EsTemplate addTemplate(EsTemplate esTemplate) {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        esTemplate.setClusterCode(clusterCode);

        validate(esTemplate);
        esTemplateMapper.insertTemplate(esTemplate);
        return esTemplate;
    }

    @Override
    public EsTemplate getTemplate(Long id) {
        return esTemplateMapper.getTemplateById(id);
    }

    @Override
    public EsTemplate updateTemplate(EsTemplate esTemplate) {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        esTemplate.setClusterCode(clusterCode);

        validate(esTemplate);
        esTemplateMapper.updateTemplate(esTemplate);
        return esTemplate;
    }

    @Override
    public void deleteTemplate(Long id) {
        esTemplateMapper.deleteTemplateById(id);
    }

    @Override
    public List<EsTemplate> getAllTemplates() {
        return esTemplateMapper.getAllTemplates();
    }

    @Override
    public JSONObject previewTemplate(EsUnconvertedTemplate esUnconvertedTemplate) {
        PreviewTemplateHandler  previewTemplateHandler = new PreviewTemplateHandler();
        return previewTemplateHandler.previewTemplate(esUnconvertedTemplate);
    }

    /**
     * 校验方法
     */
    private void validate(EsTemplate esTemplate) {
        if (esTemplate == null) {
            throw new IllegalArgumentException("EsTemplate object cannot be null.");
        }
        if (esTemplate.getClusterCode() == null || esTemplate.getClusterCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Cluster code cannot be empty.");
        }
        if (esTemplate.getZhName() == null || esTemplate.getZhName().trim().isEmpty()) {
            throw new IllegalArgumentException("Chinese name cannot be empty.");
        }
        if (esTemplate.getEnName() == null || esTemplate.getEnName().trim().isEmpty()) {
            throw new IllegalArgumentException("English name cannot be empty.");
        }
        if (esTemplate.getTemplateContent() == null || esTemplate.getTemplateContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Template content cannot be empty.");
        }
    }
}