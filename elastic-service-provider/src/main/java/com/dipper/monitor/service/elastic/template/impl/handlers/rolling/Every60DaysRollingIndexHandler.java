package com.dipper.monitor.service.elastic.template.impl.handlers.rolling;

import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.template.impl.handlers.rolling.immediately.AbstractRollingIndexByTemplateHandler;

public class Every60DaysRollingIndexHandler extends AbstractRollingIndexByTemplateHandler {
    public Every60DaysRollingIndexHandler(EsUnconvertedTemplate esUnconvertedTemplate) {
        super(esUnconvertedTemplate);
    }

    public void handle() {
        
    }
}
