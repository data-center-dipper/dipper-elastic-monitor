package com.dipper.monitor.service.elastic.template.impl.handlers.history;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.template.history.TemplateDetailView;
import com.dipper.monitor.entity.elastic.template.history.TemplateHistoryView;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public abstract class AbstractHistoryTemplateHandler {

    protected ElasticRealTemplateService elasticRealTemplateService;

    public AbstractHistoryTemplateHandler() {
        this.elasticRealTemplateService = SpringUtil.getBean(ElasticRealTemplateService.class);
    }
    
    public AbstractHistoryTemplateHandler(ElasticRealTemplateService elasticRealTemplateService) {
        this.elasticRealTemplateService = elasticRealTemplateService;
    }

    /**
     * 获取所有模板并转换为TemplateHistoryView
     *
     * @return 模板列表
     */
    protected List<TemplateHistoryView> getAllTemplates() {
        try {
            // 获取模板基本信息
            List<TemplateHistoryView> templateInfoList = elasticRealTemplateService.getTemplateHistoryViewCache();
            return templateInfoList;
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return new ArrayList<>();
        }
    }

}
