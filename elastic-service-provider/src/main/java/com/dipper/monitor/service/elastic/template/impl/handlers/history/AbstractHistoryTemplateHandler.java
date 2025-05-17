package com.dipper.monitor.service.elastic.template.impl.handlers.history;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;

public abstract class AbstractHistoryTemplateHandler {

    protected ElasticRealTemplateService elasticRealTemplateService;

    public AbstractHistoryTemplateHandler() {
        this.elasticRealTemplateService = SpringUtil.getBean(ElasticRealTemplateService.class);
    }
    
    public AbstractHistoryTemplateHandler(ElasticRealTemplateService elasticRealTemplateService) {
        this.elasticRealTemplateService = elasticRealTemplateService;
    }
}
