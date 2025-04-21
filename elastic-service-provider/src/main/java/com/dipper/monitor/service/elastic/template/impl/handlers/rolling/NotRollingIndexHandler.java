package com.dipper.monitor.service.elastic.template.impl.handlers.rolling;

import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;

/**
 * 不需要滚动的索引
 */
public class NotRollingIndexHandler extends AbstractRollingIndexByTemplateHandler {

    private EsUnconvertedTemplate esUnconvertedTemplate;

    public NotRollingIndexHandler(EsUnconvertedTemplate esUnconvertedTemplate) {
        super();
        this.esUnconvertedTemplate = esUnconvertedTemplate;
    }

    public void handle() {

    }
}
