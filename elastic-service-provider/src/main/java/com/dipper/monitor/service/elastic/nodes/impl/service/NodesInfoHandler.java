package com.dipper.monitor.service.elastic.nodes.impl.service;

import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.EsNodeInfo;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.*;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NodesInfoHandler   extends AbstractNodeDesHandler {

    private static final Logger logger = LoggerFactory.getLogger(NodesInfoHandler.class);

    private ElasticClientProxyService clientProxyService;
    
    public NodesInfoHandler(ElasticClientService elasticClientService) {
        super(elasticClientService);
        this.clientProxyService = clientProxyService;
    }



}
