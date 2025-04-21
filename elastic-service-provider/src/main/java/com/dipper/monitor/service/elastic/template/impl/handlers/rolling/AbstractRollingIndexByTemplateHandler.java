package com.dipper.monitor.service.elastic.template.impl.handlers.rolling;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.service.elastic.template.EsTemplateService;

public class AbstractRollingIndexByTemplateHandler {


    protected EsTemplateService esTemplateService;

    public AbstractRollingIndexByTemplateHandler() {
        this.esTemplateService = SpringUtil.getBean(EsTemplateService.class);
    }
}
