package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.TemplatePageInfo;
import com.dipper.monitor.entity.elastic.template.history.TemplateDetailReq;
import com.dipper.monitor.entity.elastic.template.history.TemplateHistoryView;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import com.dipper.monitor.service.elastic.template.TemplateHistoryService;
import com.dipper.monitor.service.elastic.template.impl.handlers.history.BusinessHistoryTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.history.CurrentHistoryTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.history.SystemHistoryTemplateHandler;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class TemplateHistoryServiceImpl implements TemplateHistoryService {

    @Autowired
    private ElasticRealTemplateService elasticRealTemplateService;

    /**
     * 获取当前使用模板分页信息
     * 如果模版是按年分的，那么就是当年的模版
     * 如果模版是按月分的，那么就是当月的模版
     * 如果模版是按天分的，那么就是当天的模版
     * @param templatePageInfo
     * @return
     */
    @Override
    public Tuple2<Integer, List<TemplateHistoryView>> getCurrentUseTemplatePage(TemplatePageInfo templatePageInfo) {
        CurrentHistoryTemplateHandler currentHistoryTemplateHandler = new CurrentHistoryTemplateHandler();
        return currentHistoryTemplateHandler.getCurrentUseTemplatePage(templatePageInfo);
    }

    @Override
    public Tuple2<Integer, List<TemplateHistoryView>> getBusinessUseTemplate(TemplatePageInfo templatePageInfo) {
        BusinessHistoryTemplateHandler businessHistoryTemplateHandler = new BusinessHistoryTemplateHandler();
        return businessHistoryTemplateHandler.getBusenessUseTemplate(templatePageInfo);
    }

    @Override
    public Tuple2<Integer, List<TemplateHistoryView>> getSystemUseTemplate(TemplatePageInfo templatePageInfo) {
        SystemHistoryTemplateHandler systemHistoryTemplateHandler = new SystemHistoryTemplateHandler();
        return systemHistoryTemplateHandler.getSystemUseTemplate(templatePageInfo);
    }

    @Override
    public JSONObject getOneTemplateDetail(TemplateDetailReq templateDetailReq) throws IOException {
        String templateName = templateDetailReq.getTemplateName();
        JSONObject oneTemplateDetail = elasticRealTemplateService.getOneTemplateDetail(templateName);
        return oneTemplateDetail;
    }

    @Override
    public void deleteTemplate(String templateName) throws IOException {
        elasticRealTemplateService.deleteTemplate(templateName);
    }
}
