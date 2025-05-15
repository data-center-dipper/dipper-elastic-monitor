package com.dipper.monitor.service.elastic.template.impl.handlers.rolling.immediately;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.alians.ElasticAliansService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.life.ElasticRealLifecyclePoliciesService;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.service.elastic.template.TemplatePreviewService;

public class AbstractRollingIndexByTemplateHandler {


    protected ElasticStoreTemplateService elasticStoreTemplateService;
    protected EsUnconvertedTemplate esUnconvertedTemplate;
    protected ElasticClientService elasticClientService;
    protected TemplatePreviewService templatePreviewService;
    protected ElasticRealTemplateService elasticRealTemplateService;
    protected ElasticAliansService elasticAliansService;
    protected ElasticRealLifecyclePoliciesService elasticRealLifecyclePoliciesService;
    protected ElasticRealIndexService elasticRealIndexService;

    public AbstractRollingIndexByTemplateHandler(EsUnconvertedTemplate esUnconvertedTemplate) {
        this.esUnconvertedTemplate = esUnconvertedTemplate;

        this.elasticStoreTemplateService = SpringUtil.getBean(ElasticStoreTemplateService.class);
        this.elasticClientService = SpringUtil.getBean(ElasticClientService.class);
        this.templatePreviewService = SpringUtil.getBean(TemplatePreviewService.class);
        this.elasticRealTemplateService = SpringUtil.getBean(ElasticRealTemplateService.class);
        this.elasticAliansService = SpringUtil.getBean(ElasticAliansService.class);
        this.elasticRealLifecyclePoliciesService = SpringUtil.getBean(ElasticRealLifecyclePoliciesService.class);
        this.elasticRealIndexService = SpringUtil.getBean(ElasticRealIndexService.class);
    }

    protected void getIndexTime(String indexPatterns) {

    }
}
