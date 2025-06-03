package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSON;
import com.dipper.monitor.config.template.TemplateConfig;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.template.unconverted.PrefabricateTemplateEntity;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.service.elastic.template.TemplateInnerService;
import com.dipper.monitor.service.elastic.template.impl.handlers.inner.InnerTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.prefabricate.PrefabricateTemplateInitHandler;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TemplateInnerServiceImpl implements TemplateInnerService {

    @Autowired
    private TemplateConfig templateConfig;
    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;

    private InnerTemplateHandler innerTemplateHandler;

    @PostConstruct
    public void init() {
        log.info("准备初始化模板的模板");
        try {
            CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
            String clusterVersion = currentCluster.getClusterVersion();
            innerTemplateHandler = new InnerTemplateHandler(templateConfig,clusterVersion);
            innerTemplateHandler.initTemplate();
        }catch (Exception e){
            log.error("初始化模板的模板失败",e);
        }
        try {
            doSave();
        }catch (Exception e){
            log.error("初始化预制模板失败",e);
        }
    }

    private void doSave() {
        List<PrefabricateTemplateEntity> esUnconvertedTemplateList = innerTemplateHandler.getEsUnconvertedTemplateList();
        if (esUnconvertedTemplateList == null || esUnconvertedTemplateList.isEmpty()) {
            log.info("预制模板为空");
            return;
        }


        List<EsTemplateEntity> allTemplates = transToEntity(esUnconvertedTemplateList);
        List<EsTemplateEntity> dbTemplates = elasticStoreTemplateService.getAllTemplates();

        // 将数据库中的模板转为 Map 结构，避免双循环
        Map<String, EsTemplateEntity> dbTemplateMap = new HashMap<>();
        if (dbTemplates != null && !dbTemplates.isEmpty()) {
            dbTemplateMap = dbTemplates.stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.getZhName() != null)
                    .collect(Collectors.toMap(
                            EsTemplateEntity::getZhName,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));
        }

        // 筛选出需要新增的模板
        List<EsTemplateEntity> toBeSaved = new ArrayList<>();
        for (EsTemplateEntity template : allTemplates) {
            if (template.getZhName() != null && !dbTemplateMap.containsKey(template.getZhName())) {
                toBeSaved.add(template);
            }
        }

        // 执行批量保存
        if (!toBeSaved.isEmpty()) {
            boolean success = elasticStoreTemplateService.batchInsertTemplates(toBeSaved);
            if (success) {
                log.info("成功新增 {} 个新模板", toBeSaved.size());
            } else {
                log.error("新增模板失败");
            }
        } else {
            log.info("没有需要新增的模板");
        }
    }

    private List<EsTemplateEntity> transToEntity(List<PrefabricateTemplateEntity> esUnconvertedTemplateList) {
        List<EsTemplateEntity> result = new ArrayList<>();

        if (esUnconvertedTemplateList == null || esUnconvertedTemplateList.isEmpty()) {
            return result;
        }
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();

        for (PrefabricateTemplateEntity source : esUnconvertedTemplateList) {
            EsTemplateEntity target = new EsTemplateEntity();

            // 基础字段直接映射
            target.setZhName(source.getZhName());
            target.setEnName(source.getEnName());
            target.setDicName(source.getDicName());
            target.setIndexPatterns(source.getIndexPatterns());
            target.setAliansPatterns(source.getAliansPatterns());
            target.setNumberOfShards(source.getNumberOfShards());
            target.setNumberOfReplicas(source.getNumberOfReplicas());
            target.setEnableAutoShards(source.getEnableAutoShards());
            target.setRollingPeriod(source.getRollingPeriod());

            // 设置信息：Map -> String
            if (source.getSettings() != null) {
                target.setSettings(JSON.toJSONString(source.getSettings()));
            } else {
                target.setSettings(null);
            }

            // templateContent: JSONObject -> String
            if (source.getTemplateContent() != null) {
                target.setTemplateContent(source.getTemplateContent().toJSONString());
            } else {
                target.setTemplateContent(null);
            }

            // statMessage 可选填充（示例，根据需求修改）
            target.setStatMessage("模板已构建");

            // 时间字段（假设使用当前时间或可从 source 获取）
            target.setCreateTime(new Date());
            target.setUpdateTime(new Date());

            // clusterCode 如果有逻辑可设置，否则留空或传参进来
            target.setClusterCode(clusterCode);

            // lifePolicy 示例值，实际应根据业务决定
            target.setLifePolicy("default_policy");

            result.add(target);
        }

        return result;
    }

}
