package com.dipper.monitor.service.elastic.template;

import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.entity.elastic.template.unconverted.PrefabricateTemplateNames;

import java.util.List;

public interface ElasticPrefabricateTemplateService {
    /**
     * 获取预制模板列表
     * @return
     */
    List<EsUnconvertedTemplate> prefabricateTemplate();

    /**
     * 获取预制模板名称列表
     * @return
     */
    List<PrefabricateTemplateNames> prefabricateTemplateNames();

    /**
     * 获取单个预制模板
     * @param enName
     * @return
     */
    EsUnconvertedTemplate prefabricateOneTemplate(String enName);
}
