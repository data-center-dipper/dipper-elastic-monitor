package com.dipper.monitor.service.elastic.template.impl.handlers.rolling;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;

public class AbstractRollingIndexByTemplateHandler {


    protected ElasticStoreTemplateService elasticStoreTemplateService;

    public AbstractRollingIndexByTemplateHandler() {
        this.elasticStoreTemplateService = SpringUtil.getBean(ElasticStoreTemplateService.class);
    }

    protected void getIndexTime(String indexPatterns) {

    }
}
