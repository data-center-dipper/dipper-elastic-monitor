package com.dipper.monitor.service.elastic.life.impl.service;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.service.elastic.alians.ElasticAliasService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.life.ElasticRealLifecyclePoliciesService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractLifeCycleError {

    protected static final String HENGXIAN = "\r\n---------------------------------------------------------\r\n";

    protected StringBuilder builder = new StringBuilder(3000);

    protected ElasticRealLifecyclePoliciesService elasticRealLifecyclePoliciesService;
    protected ElasticClientService elasticClientService;
    protected ElasticAliasService elasticAliasService;

    public AbstractLifeCycleError(){
        elasticRealLifecyclePoliciesService = SpringUtil.getBean(ElasticRealLifecyclePoliciesService.class);
        elasticClientService = SpringUtil.getBean(ElasticClientService.class);
        elasticAliasService = SpringUtil.getBean(ElasticAliasService.class);

        init();
    }

    public void init() {
    }

}
