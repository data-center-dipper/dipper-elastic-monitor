package com.dipper.monitor.service.elastic.template.impl.handlers.rolling;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.service.elastic.template.ElasticTemplateService;

public class AbstractRollingIndexByTemplateHandler {


    protected ElasticTemplateService elasticTemplateService;

    public AbstractRollingIndexByTemplateHandler() {
        this.elasticTemplateService = SpringUtil.getBean(ElasticTemplateService.class);
    }

    protected void getIndexTime(String indexPatterns) {

    }
}
