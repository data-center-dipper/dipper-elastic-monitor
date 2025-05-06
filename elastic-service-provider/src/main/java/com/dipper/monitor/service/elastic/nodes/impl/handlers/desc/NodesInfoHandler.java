package com.dipper.monitor.service.elastic.nodes.impl.handlers.desc;

import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodesInfoHandler   extends AbstractNodeDesHandler {

    private static final Logger logger = LoggerFactory.getLogger(NodesInfoHandler.class);

    private ElasticClientProxyService clientProxyService;
    
    public NodesInfoHandler(ElasticClientService elasticClientService) {
        super(elasticClientService);
        this.clientProxyService = clientProxyService;
    }



}
