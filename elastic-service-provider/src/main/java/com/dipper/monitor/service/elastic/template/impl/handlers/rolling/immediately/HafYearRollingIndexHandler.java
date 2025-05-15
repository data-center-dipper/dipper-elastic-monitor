package com.dipper.monitor.service.elastic.template.impl.handlers.rolling.immediately;

import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;

/**
 * 半年滚动的模版
 * 索引模版 格式 lcc-logs-yyyyMM-*
 * 上半年 1-6月
 *       格式 lcc-logs-yyyyMM-*
 *       索引
 *          lcc-logs-202501-0000001
 *          lcc-logs-202501-0000002
 *          lcc-logs-202501-0000003
 *      上半年滚动 后缀自增
 * 下半年 7-12月
 *       格式 lcc-logs-yyyyMM-*
 *       索引
 *          lcc-logs-202506-0000001
 *          lcc-logs-202506-0000002
 *          lcc-logs-202506-0000003
 *          下半年滚动 后缀自增
 */
public class HafYearRollingIndexHandler extends AbstractRollingIndexByTemplateHandler {
    public HafYearRollingIndexHandler(EsUnconvertedTemplate esUnconvertedTemplate) {
        super(esUnconvertedTemplate);
    }

    public void handle() {
        
    }
}
