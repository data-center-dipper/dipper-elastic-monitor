package com.dipper.monitor.service.elastic.policy.impl;

import com.alibaba.fastjson.JSON;
import com.dipper.monitor.config.template.TemplateConfig;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.db.elastic.LifePolicyEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.policy.InnerPolicyEntity;
import com.dipper.monitor.entity.elastic.policy.response.LifePolicyResponse;
import com.dipper.monitor.entity.elastic.template.unconverted.PrefabricateTemplateEntity;
import com.dipper.monitor.service.elastic.policy.InnerPolicyService;
import com.dipper.monitor.service.elastic.policy.LifePolicyStoreService;
import com.dipper.monitor.service.elastic.policy.handler.InnerPolicyHandler;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.service.elastic.template.impl.handlers.inner.InnerTemplateHandler;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InnerPolicyServiceImpl implements InnerPolicyService {


    @Autowired
    private TemplateConfig templateConfig;
    @Autowired
    private LifePolicyStoreService lifePolicyStoreService;

    private InnerPolicyHandler innerPolicyHandler;

    @PostConstruct
    public void init() {
        log.info("准备初始化模板的模板");
        try {
            innerPolicyHandler = new InnerPolicyHandler(templateConfig);
            innerPolicyHandler.initPolicy();
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
        List<InnerPolicyEntity> esPolicyList = innerPolicyHandler.getEsPolicyList();
        if (esPolicyList == null || esPolicyList.isEmpty()) {
            log.info("预制模板为空");
            return;
        }

        List<LifePolicyEntity> allTemplates = transToEntity(esPolicyList);
        List<LifePolicyEntity> dbTemplates = lifePolicyStoreService.getAllPolicieEntitys();

        // 将数据库中的模板转为 Map 结构，避免双循环
        Map<String, LifePolicyEntity> dbTemplateMap = new HashMap<>();
        if (dbTemplates != null && !dbTemplates.isEmpty()) {
            dbTemplateMap = dbTemplates.stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.getZhName() != null)
                    .collect(Collectors.toMap(
                            LifePolicyEntity::getZhName,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));
        }

        // 筛选出需要新增的模板
        List<LifePolicyEntity> toBeSaved = new ArrayList<>();
        for (LifePolicyEntity template : allTemplates) {
            if (template.getZhName() != null && !dbTemplateMap.containsKey(template.getZhName())) {
                toBeSaved.add(template);
            }
        }

        // 执行批量保存
        if (!toBeSaved.isEmpty()) {
            boolean success = lifePolicyStoreService.batchInsertTemplates(toBeSaved);
            if (success) {
                log.info("成功新增 {} 个新模板", toBeSaved.size());
            } else {
                log.error("新增模板失败");
            }
        } else {
            log.info("没有需要新增的模板");
        }
    }

    private List<LifePolicyEntity> transToEntity(List<InnerPolicyEntity> esPolicyList) {
        List<LifePolicyEntity> result = new ArrayList<>();

        if (esPolicyList == null || esPolicyList.isEmpty()) {
            return result;
        }
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();

        for (InnerPolicyEntity source : esPolicyList) {
            LifePolicyEntity entity = new LifePolicyEntity();
            entity.setClusterCode(clusterCode);
            entity.setZhName(source.getZhName());
            entity.setEnName(source.getEnName());
            entity.setPolicyValue(source.getPolicyContent().toJSONString());
            entity.setClusterCode(clusterCode);

            result.add(entity);
        }

        return result;
    }
}
