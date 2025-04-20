package com.dipper.monitor.service.elastic.nodes.impl.service;

import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.db.elastic.NodeStoreEntity;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.EsNodeInfo;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.*;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import com.dipper.monitor.service.elastic.nodes.impl.ElasticRealRealNodeServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OneNodeInfoHandler  extends AbstractNodeDesHandler {

    private static final Logger logger = LoggerFactory.getLogger(OneNodeInfoHandler.class);

    private NodeStoreEntity nodeStoreEntity;
    private ElasticClientService elasticClientService;


    public OneNodeInfoHandler(NodeStoreEntity nodeStoreEntity,
                              ElasticClientService elasticClientService) {
        super(elasticClientService);
        this.nodeStoreEntity = nodeStoreEntity;
        this.elasticClientService = SpringUtil.getBean(ElasticClientService.class);;
    }



    public EsNodeInfo getOneNodeDetail() throws IOException {
        String hostName = nodeStoreEntity.getHostName();
        String httpResult = elasticClientService.executeGetApi("/_nodes/"+hostName);

        List<EsNodeInfo> esNodeInfos = parseReponse(httpResult);

        if(esNodeInfos == null || esNodeInfos.size() == 0){
            return null;
        }
        return esNodeInfos.get(0);
    }

}
