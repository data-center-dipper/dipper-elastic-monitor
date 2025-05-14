package com.dipper.monitor.service.elastic.template.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.service.elastic.template.TemplatePreviewService;
import com.dipper.monitor.service.elastic.template.impl.handlers.preview.PreviewCanRunTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.preview.PreviewTemplateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplatePreviewServiceImpl implements TemplatePreviewService {

    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;

    @Override
    public JSONObject previewTemplate(EsUnconvertedTemplate esUnconvertedTemplate) {
        PreviewTemplateHandler previewTemplateHandler = new PreviewTemplateHandler();
        return previewTemplateHandler.previewTemplate(esUnconvertedTemplate);
    }

    /**
     * 预览模板,可以直接使用的模版信息
     *
     * @param id
     * @return
     */
    @Override
    public JSONObject previewEffectTemplate(Integer id) {
        EsUnconvertedTemplate oneUnconvertedTemplate = elasticStoreTemplateService.getOneUnconvertedTemplate(id);
        if(oneUnconvertedTemplate == null) {
            throw new RuntimeException("模板不存在");
        }
        JSONObject jsonObject = previewTemplate(oneUnconvertedTemplate);
        PreviewCanRunTemplateHandler previewCanRunTemplateHandler = new PreviewCanRunTemplateHandler();
        return previewCanRunTemplateHandler.previewCanRunTemplate(jsonObject);
    }

}
