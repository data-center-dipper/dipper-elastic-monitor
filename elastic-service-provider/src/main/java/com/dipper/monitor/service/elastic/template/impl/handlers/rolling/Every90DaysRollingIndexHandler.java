package com.dipper.monitor.service.elastic.template.impl.handlers.rolling;

import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.template.impl.handlers.rolling.immediately.AbstractRollingIndexByTemplateHandler;

/**
 * 半年滚动一次的索引
 *
 */
public class Every90DaysRollingIndexHandler extends AbstractRollingIndexByTemplateHandler {
    public Every90DaysRollingIndexHandler(EsUnconvertedTemplate esUnconvertedTemplate) {
        super(esUnconvertedTemplate);
    }

    public void handle() {
    }
}
