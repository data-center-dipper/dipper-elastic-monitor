package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.life.EsTemplateStatEntity;
import com.dipper.monitor.entity.elastic.template.*;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.mapper.EsTemplateMapper;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.service.elastic.template.impl.handlers.preview.Preview8xTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.shard.TemplateShardHistoryHandler;
import com.dipper.monitor.utils.DateDipperUtil;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import com.dipper.monitor.utils.mock.MockAllData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ElasticStoreTemplateServiceImpl implements ElasticStoreTemplateService {

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
        BeanUtils.copyProperties(esUnconvertedTemplate, esTemplate);
        esTemplate.setClusterCode(clusterCode);
        esTemplate.setCreateTime(new Date());
        esTemplate.setUpdateTime(new Date());
        if (settings == null || settings.isEmpty()) {
            esTemplate.setSettings("{}");
        } else {
            JSONObject jsonObject = new JSONObject(settings);
            esTemplate.setSettings(jsonObject.toJSONString());
        }

        validate(esTemplate);

        EsTemplateEntity db = esTemplateMapper.getTemplateByEnName(clusterCode, enName);
        if (db != null) {
            esTemplateMapper.updateTemplate(esTemplate);
        } else {
            esTemplateMapper.insertTemplate(esTemplate);
        }

        return esTemplate;
    }

    @Override
    public EsTemplateEntity getTemplate(Integer id) {
        return esTemplateMapper.getTemplateById(id);
    }

    @Override
    public EsTemplateEntity updateTemplate(EsUnconvertedTemplate esUnconvertedTemplate) {
        // 转换成 EsTemplateEntity
        EsTemplateEntity esTemplateEntity = convertToEsTemplateEntity(esUnconvertedTemplate);

        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        esTemplateEntity.setClusterCode(clusterCode);

        validate(esTemplateEntity);
        esTemplateMapper.updateTemplate(esTemplateEntity);
        return esTemplateEntity;
    }

    /**
     * 将未转换的模板转换为模板实体
     *
     * @param esUnconvertedTemplate 未转换的模板
     * @return 模板实体
     */
    private EsTemplateEntity convertToEsTemplateEntity(EsUnconvertedTemplate esUnconvertedTemplate) {
        EsTemplateEntity esTemplateEntity = new EsTemplateEntity();
        BeanUtils.copyProperties(esUnconvertedTemplate, esTemplateEntity);

        // 设置基本属性
        esTemplateEntity.setId(esUnconvertedTemplate.getId());
        esTemplateEntity.setZhName(esUnconvertedTemplate.getZhName());
        esTemplateEntity.setEnName(esUnconvertedTemplate.getEnName());
        esTemplateEntity.setDicName(esUnconvertedTemplate.getDicName());
        esTemplateEntity.setIndexPatterns(esUnconvertedTemplate.getIndexPatterns());
        esTemplateEntity.setAliansPatterns(esUnconvertedTemplate.getAliansPatterns());
        esTemplateEntity.setNumberOfShards(esUnconvertedTemplate.getNumberOfShards());
        esTemplateEntity.setNumberOfReplicas(esUnconvertedTemplate.getNumberOfReplicas());
        esTemplateEntity.setEnableAutoShards(esUnconvertedTemplate.getEnableAutoShards());

        // 设置模板内容
        esTemplateEntity.setTemplateContent(esUnconvertedTemplate.getTemplateContent());

        // 设置其他必要属性
        // 如果有其他需要转换的属性，在这里添加

        return esTemplateEntity;
    }

    @Override
    public void deleteTemplate(Long id) {
        esTemplateMapper.deleteTemplateById(id);
    }

    @Override
    public List<EsTemplateEntity> getAllTemplates() {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        return esTemplateMapper.getAllTemplates(clusterCode);
    }

    @Override
    public void updateTemplateStat(List<EsTemplateStatEntity> templateStat) {
        for (EsTemplateStatEntity item : templateStat) {
            Integer id = item.getId();
            String statMessage = JSONObject.toJSONString(item);
            esTemplateMapper.updateTemplateStat(id, statMessage);
        }
    }

    @Override
    public ElasticTemplateView getTemplateAndStat(Integer id) {
        EsTemplateEntity template = getTemplate(id);
        if (template == null) {
            throw new IllegalArgumentException("template not exist");
        }
        ElasticTemplateView elasticTemplateView = new ElasticTemplateView();
        BeanUtils.copyProperties(template, elasticTemplateView);
        return elasticTemplateView;
    }

    @Override
    public Integer getTemplateNum(TemplatePageInfo templatePageInfo) {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        String keyword = templatePageInfo.getKeyword();
        return esTemplateMapper.getTemplateNum(clusterCode, keyword);
    }

    @Override
    public List<ElasticTemplateListView> getTemplateListViewByPage(TemplatePageInfo templatePageInfo) {
        List<EsTemplateEntity> templateByPage = getTemplateByPage(templatePageInfo);
        if (CollectionUtils.isEmpty(templateByPage)) {
            return Collections.emptyList();
        }
        List<ElasticTemplateListView> elasticTemplateViews = new ArrayList<>();
        for (EsTemplateEntity esTemplateEntity : templateByPage) {
            ElasticTemplateListView elasticTemplateView = new ElasticTemplateListView();
            BeanUtils.copyProperties(esTemplateEntity, elasticTemplateView);
            elasticTemplateView.setUpdateTime(DateDipperUtil.formatDate(esTemplateEntity.getUpdateTime()));
            elasticTemplateView.setId(esTemplateEntity.getId());
            elasticTemplateViews.add(elasticTemplateView);
        }
        return elasticTemplateViews;
    }

    @Override
    public EsUnconvertedTemplate getOneUnconvertedTemplate(Integer id) {
        EsTemplateEntity templateById = esTemplateMapper.getTemplateById(id);
        // 转成 EsUnconvertedTemplate
        if (templateById == null) {
            return null;
        }

        EsUnconvertedTemplate esUnconvertedTemplate = transToEsUnconvertedTemplate(templateById);
        return esUnconvertedTemplate;
    }

    private EsUnconvertedTemplate transToEsUnconvertedTemplate(EsTemplateEntity templateById) {
        EsUnconvertedTemplate unconvertedTemplate = new EsUnconvertedTemplate();

        // 复制基本属性
        unconvertedTemplate.setId(templateById.getId());
        unconvertedTemplate.setZhName(templateById.getZhName());
        unconvertedTemplate.setEnName(templateById.getEnName());
        unconvertedTemplate.setDicName(templateById.getDicName());
        unconvertedTemplate.setIndexPatterns(templateById.getIndexPatterns());
        unconvertedTemplate.setAliansPatterns(templateById.getAliansPatterns());
        unconvertedTemplate.setNumberOfShards(templateById.getNumberOfShards());
        unconvertedTemplate.setNumberOfReplicas(templateById.getNumberOfReplicas());
        unconvertedTemplate.setRollingPeriod(templateById.getRollingPeriod());
        unconvertedTemplate.setEnableAutoShards(templateById.getEnableAutoShards());
        unconvertedTemplate.setLifePolicy(templateById.getLifePolicy());

        // 设置模板内容
        unconvertedTemplate.setTemplateContent(templateById.getTemplateContent());

        // 处理设置信息
        String settings = templateById.getSettings();
        if (StringUtils.isNotBlank(settings)) {
            try {
                Map<String, Object> settingsMap = JSONObject.parseObject(settings, Map.class);
                unconvertedTemplate.setSettings(settingsMap);
            } catch (Exception e) {
                // 解析失败时设置空的设置
                unconvertedTemplate.setSettings(new HashMap<>());
            }
        } else {
            unconvertedTemplate.setSettings(new HashMap<>());
        }

        return unconvertedTemplate;
    }

    @Override
    public EsUnconvertedTemplate getOneUnconvertedTemplateByEnName(String enName) {
        if (StringUtils.isBlank(enName)) {
            throw new IllegalArgumentException("enName is blank");
        }
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        EsTemplateEntity template = esTemplateMapper.getTemplateByEnName(clusterCode, enName);
        if (template == null) {
            throw new IllegalArgumentException("template not found");
        }

        EsUnconvertedTemplate esUnconvertedTemplate = transToEsUnconvertedTemplate(template);
        return esUnconvertedTemplate;
    }

    @Override
    public EsTemplateStatEntity templateStat(Integer id) {
        if (ApplicationUtils.isWindows()) {
            return MockAllData.templateStat(id);
        }
        EsTemplateEntity template = getTemplate(id);
        if (template == null) {
            throw new IllegalArgumentException("template not exist");
        }
        String statMessage = template.getStatMessage();
        if (StringUtils.isBlank(statMessage)) {
            return null;
        }
        EsTemplateStatEntity esTemplateStatEntity = JSONObject.parseObject(statMessage, EsTemplateStatEntity.class);
        return esTemplateStatEntity;
    }

    @Override
    public List<String> templateNames(String nameLike) {
        TemplatePageInfo templatePageInfo = new TemplatePageInfo();
        templatePageInfo.setPageNum(1);
        templatePageInfo.setPageSize(20);
        templatePageInfo.setKeyword(nameLike);

        List<EsTemplateEntity> allTemplates = getTemplateByPage(templatePageInfo);
        if (CollectionUtils.isEmpty(allTemplates)) {
            return List.of();
        }
        return allTemplates.stream()
                .map(EsTemplateEntity::getEnName)
                .collect(Collectors.toList());
    }

    @Override
    public boolean batchInsertTemplates(List<EsTemplateEntity> toBeSaved) {
        if (CollectionUtils.isEmpty(toBeSaved)) {
            return false;
        }
        for (EsTemplateEntity esTemplateEntity : toBeSaved) {
            esTemplateMapper.insertTemplate(esTemplateEntity);
        }
        return true;
    }

    @Override
    public void updateAutoCreate(AutoCreateReq autoCreateReq) {
        esTemplateMapper.updateAutoCreate(autoCreateReq);
    }

    @Override
    public List<ShardHistoryItem> getTemplateShardHistory(Integer templateId) throws IOException {
        TemplateShardHistoryHandler templateShardHistoryHandler = new TemplateShardHistoryHandler(this);
        return templateShardHistoryHandler.getTemplateShardHistory(templateId);
    }


    @Override
    public List<EsTemplateEntity> getTemplateByPage(TemplatePageInfo templatePageInfo) {
        Integer pageNum = templatePageInfo.getPageNum();
        Integer pageSize = templatePageInfo.getPageSize();
        String keyword = templatePageInfo.getKeyword();
        Integer offset = (pageNum - 1) * pageSize; // 计算offset
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        return esTemplateMapper.getTemplateByPage(clusterCode, keyword, pageSize, offset);
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