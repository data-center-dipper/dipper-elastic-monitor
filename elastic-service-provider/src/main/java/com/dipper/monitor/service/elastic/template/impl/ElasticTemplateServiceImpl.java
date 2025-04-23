package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.mapper.EsTemplateMapper;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.service.elastic.template.ElasticTemplateService;
import com.dipper.monitor.service.elastic.template.impl.handlers.PreviewTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.RollingIndexByTemplateHandler;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ElasticTemplateServiceImpl implements ElasticTemplateService {

    @Autowired
    private EsTemplateMapper esTemplateMapper;
    @Autowired
    private ElasticHealthService elasticHealthService;


    @Override
    public EsTemplateEntity addOrUpdateTemplate(EsUnconvertedTemplate esUnconvertedTemplate) {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();

        String enName = esUnconvertedTemplate.getEnName();
        Map<String, Object> settings = esUnconvertedTemplate.getSettings();

        EsTemplateEntity esTemplate = new EsTemplateEntity();
        BeanUtils.copyProperties(esUnconvertedTemplate,esTemplate);
        esTemplate.setClusterCode(clusterCode);
        esTemplate.setCreateTime(new Date());
        esTemplate.setUpdateTime(new Date());
        if(settings == null || settings.isEmpty()){
            esTemplate.setSettings("{}");
        }else {
            JSONObject jsonObject = new JSONObject(settings);
            esTemplate.setSettings(jsonObject.toJSONString());
        }

        validate(esTemplate);

        EsTemplateEntity db = esTemplateMapper.getTemplateByEnName(clusterCode,enName);
        if(db != null){
            esTemplateMapper.updateTemplate(esTemplate);
        }else {
            esTemplateMapper.insertTemplate(esTemplate);
        }

        return esTemplate;
    }

    @Override
    public EsTemplateEntity getTemplate(Long id) {
        return esTemplateMapper.getTemplateById(id);
    }

    @Override
    public EsTemplateEntity updateTemplate(EsTemplateEntity esTemplateEntity) {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        esTemplateEntity.setClusterCode(clusterCode);

        validate(esTemplateEntity);
        esTemplateMapper.updateTemplate(esTemplateEntity);
        return esTemplateEntity;
    }

    @Override
    public void deleteTemplate(Long id) {
        esTemplateMapper.deleteTemplateById(id);
    }

    @Override
    public List<EsTemplateEntity> getAllTemplates() {
        return esTemplateMapper.getAllTemplates();
    }

    @Override
    public JSONObject previewTemplate(EsUnconvertedTemplate esUnconvertedTemplate) {
        PreviewTemplateHandler  previewTemplateHandler = new PreviewTemplateHandler();
        return previewTemplateHandler.previewTemplate(esUnconvertedTemplate);
    }

    @Override
    public void addAndRollTemplate(EsUnconvertedTemplate esUnconvertedTemplate) {
        addOrUpdateTemplate(esUnconvertedTemplate);
        rollTemplate(esUnconvertedTemplate);

    }

    @Override
    public void rollTemplate(EsUnconvertedTemplate esUnconvertedTemplate) {
        RollingIndexByTemplateHandler rollingIndexByTemplateHandler = new RollingIndexByTemplateHandler(elasticHealthService);
        rollingIndexByTemplateHandler.rollIndexByTemplate(esUnconvertedTemplate);
    }

    /**
     * 校验方法
     */
    private void validate(EsTemplateEntity esTemplateEntity) {
        if (esTemplateEntity == null) {
            throw new IllegalArgumentException("EsTemplate object cannot be null.");
        }
        if (esTemplateEntity.getClusterCode() == null || esTemplateEntity.getClusterCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Cluster code cannot be empty.");
        }
        if (esTemplateEntity.getZhName() == null || esTemplateEntity.getZhName().trim().isEmpty()) {
            throw new IllegalArgumentException("Chinese name cannot be empty.");
        }
        if (esTemplateEntity.getEnName() == null || esTemplateEntity.getEnName().trim().isEmpty()) {
            throw new IllegalArgumentException("English name cannot be empty.");
        }
        if (esTemplateEntity.getTemplateContent() == null || esTemplateEntity.getTemplateContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Template content cannot be empty.");
        }
    }
}