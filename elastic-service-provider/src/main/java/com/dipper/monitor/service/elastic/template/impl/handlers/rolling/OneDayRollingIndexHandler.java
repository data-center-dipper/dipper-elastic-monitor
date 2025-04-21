package com.dipper.monitor.service.elastic.template.impl.handlers.rolling;

import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;

/**
 * 按照天滚动的索引
 */
public class OneDayRollingIndexHandler  extends AbstractRollingIndexByTemplateHandler {

    public OneDayRollingIndexHandler() {
        super();
    }

    public OneDayRollingIndexHandler(EsUnconvertedTemplate esUnconvertedTemplate) {
    }

    public void handle() {
    }
}
