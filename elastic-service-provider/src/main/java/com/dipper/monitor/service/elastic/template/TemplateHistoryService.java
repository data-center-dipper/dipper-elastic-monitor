package com.dipper.monitor.service.elastic.template;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.TemplatePageInfo;
import com.dipper.monitor.entity.elastic.template.history.TemplateDetailReq;
import com.dipper.monitor.entity.elastic.template.history.TemplateHistoryView;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.util.List;

/**
 * 历史模版相关
 */
public interface TemplateHistoryService {
    /**
     * 获取当前使用模版分页
     * @param templatePageInfo
     * @return
     */
    Tuple2<Integer, List<TemplateHistoryView>> getCurrentUseTemplatePage(TemplatePageInfo templatePageInfo);

    /**
     * 获取当前使用模版数量
     * @param templatePageInfo
     * @return
     */
    Tuple2<Integer, List<TemplateHistoryView>> getBusenessUseTemplate(TemplatePageInfo templatePageInfo);

    /**
     * 获取当前使用的系统模版
     * @param templatePageInfo
     * @return
     */
    Tuple2<Integer, List<TemplateHistoryView>> getSystemUseTemplate(TemplatePageInfo templatePageInfo);

    /**
     * 获取模版详情
     * @param templateName
     * @return
     */
    JSONObject getOneTemplateDetail(TemplateDetailReq templateDetailReq) throws IOException;
}
