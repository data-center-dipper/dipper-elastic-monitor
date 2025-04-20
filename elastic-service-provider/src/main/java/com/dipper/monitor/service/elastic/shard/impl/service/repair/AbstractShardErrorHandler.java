package com.dipper.monitor.service.elastic.shard.impl.service.repair;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.service.elastic.alians.ElasticAliansService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.disk.ElasticDiskService;
import com.dipper.monitor.service.elastic.life.LifecyclePoliciesService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeService;
import com.dipper.monitor.service.elastic.shard.ShardService;

public abstract class AbstractShardErrorHandler {


    protected LifecyclePoliciesService lifecyclePoliciesService;
    protected ShardService shardService;
    protected ElasticClientService elasticClientService;
    protected ElasticAliansService elasticAliansService;
    protected ElasticNodeService elasticNodeService;
    protected ElasticDiskService elasticDiskService;

    protected static final String HENGXIAN = "\r\n---------------------------------------------------------\r\n";

    protected StringBuilder builder = new StringBuilder(3000);

    public AbstractShardErrorHandler(){
        lifecyclePoliciesService = SpringUtil.getBean(LifecyclePoliciesService.class);
        shardService = SpringUtil.getBean(ShardService.class);
        elasticClientService = SpringUtil.getBean(ElasticClientService.class);
        elasticAliansService = SpringUtil.getBean(ElasticAliansService.class);
        elasticNodeService = SpringUtil.getBean(ElasticNodeService.class);
        elasticDiskService = SpringUtil.getBean(ElasticDiskService.class);

        init();
    }

    public void init() {
    }
}
