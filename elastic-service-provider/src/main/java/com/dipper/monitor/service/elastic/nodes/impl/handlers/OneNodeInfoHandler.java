package com.dipper.monitor.service.elastic.nodes.impl.handlers;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.db.elastic.NodeStoreEntity;
import com.dipper.monitor.entity.elastic.original.nodes.info.EsNodeInfo;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
