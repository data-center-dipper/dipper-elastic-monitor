package com.dipper.monitor.service.elastic.nodes.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.db.elastic.NodeStoreEntity;
import com.dipper.monitor.entity.elastic.LineChartDataResponse;
import com.dipper.monitor.entity.elastic.nodes.*;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDetail;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.entity.elastic.nodes.service.EsNodeFailed;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.EsNodeInfo;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import com.dipper.monitor.service.elastic.nodes.impl.handlers.*;
import com.dipper.monitor.service.elastic.nodes.impl.handlers.charts.NodeCharsHandler;
import com.dipper.monitor.utils.Tuple2;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ElasticRealRealNodeServiceImpl implements ElasticRealNodeService {

    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticNodeStoreService elasticNodeStoreService;


    @Override
    public void refreshNodes() {
        try {
            ElasticClusterManagerService managerService = SpringUtil.getBean(ElasticClusterManagerService.class);
            CurrentClusterEntity currentCluster = managerService.getCurrentCluster();

            log.info("准备刷新节点数据");
            List<EsNodeInfo> esNodes = getEsNodes();
            if(CollectionUtil.isEmpty(esNodes)) {
                return;
            }

            List<NodeStoreEntity> nodeStoreEntities = new ArrayList<>();
            for (EsNodeInfo esNodeInfo : esNodes) {
                String hostName = esNodeInfo.getName();
                // es 这两个字段，可能没有什么用 这是内部ip 如果这个是docker 那么这个没用
                String host = esNodeInfo.getHost();
                String ip = esNodeInfo.getIp();

                NodeStoreEntity nodeStoreEntity = new NodeStoreEntity();
                nodeStoreEntity.setClusterCode(currentCluster.getClusterCode());
                nodeStoreEntity.setHostName(hostName);
                nodeStoreEntity.setHostIp(ip);
                // 设置默认的端口
                nodeStoreEntity.setHostPort(9200);
                nodeStoreEntity.setAddress(hostName);

                nodeStoreEntities.add(nodeStoreEntity);
            }


            elasticNodeStoreService.saveOrUpdateBrokerStore(currentCluster.getClusterCode(),nodeStoreEntities);
        }catch (Exception e){
            log.error("刷新节点数据失败",e);
        }
    }

    @Override
    public List<EsNodeInfo> getEsNodes() throws IOException {
        log.info("准备刷新节点数据");
        NodesInfoHandler nodesInfoHandler = new NodesInfoHandler(elasticClientService);
        List<EsNodeInfo> esNodes = nodesInfoHandler.getEsNodes();
        return esNodes;
    }

    @Override
    public EsNodeFailed getEsNodeFailed() throws IOException {
        String nodeState = elasticClientService.executeGetApi(ElasticRestApi.NODES_LIST.getApiPath());
        JSONObject nodeStateJson = JSON.parseObject(nodeState);
        Integer nodesTotal = (Integer) JSONPath.eval(nodeStateJson, "$._nodes.total");
        Integer nodesSuccessful = (Integer)JSONPath.eval(nodeStateJson, "$._nodes.successful");
        Integer nodesFailed = (Integer)JSONPath.eval(nodeStateJson, "$._nodes.failed");

        EsNodeFailed esNodeFailed = new EsNodeFailed();
        esNodeFailed.setNodesTotal(nodesTotal);
        esNodeFailed.setNodesSuccessful(nodesSuccessful);
        esNodeFailed.setNodesFailed(nodesFailed);
        return esNodeFailed;
    }

    @Override
    public  List<ElasticNodeDetail>  nodeMemoryTop10() throws IOException {
        ListHighMemoryRiskNodesHandler listHighMemoryRiskNodesHandler = new ListHighMemoryRiskNodesHandler(elasticClientService);
        return listHighMemoryRiskNodesHandler.listHighRiskNodes();
    }

    @Override
    public  List<ElasticNodeDisk>  nodeDiskTop10() throws IOException {
        ListHighDiskRiskNodesHandler listHighMemoryRiskNodesHandler = new ListHighDiskRiskNodesHandler(elasticClientService);
        return listHighMemoryRiskNodesHandler.listHighDiskRiskNodes();
    }

    @Override
    public Tuple2<List<OneNodeTabView>, Integer> nodePageList(NodeInfoReq nodeInfoReq) throws IOException {
        NodeListHandler nodeListHandler = new NodeListHandler(nodeInfoReq,elasticNodeStoreService,this);
        Tuple2<List<OneNodeTabView>, Integer> listIntegerTuple2 = nodeListHandler.nodePageList();
        return listIntegerTuple2;
    }


    @Override
    public NodeDetailView getOneNodeView(Integer nodeId) throws IOException {
        NodeDescHandler nodeInfoHandler = new NodeDescHandler(nodeId,elasticNodeStoreService,this);
        NodeDetailView detailView = nodeInfoHandler.getNodeDetail();
        return detailView;
    }

    @Override
    public JSONObject getOneNodeOriginal(Integer nodeId) throws IOException {
        // 从服务获取节点存储实体
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        NodeStoreEntity nodeStoreEntity = elasticNodeStoreService.getByNodeId(currentCluster, nodeId);
        if (nodeStoreEntity == null) {
            log.warn("未找到节点ID为 {} 的节点信息", nodeId);
            return null;
        }
        // 获取节点详细信息
        String hostName = nodeStoreEntity.getHostName();
        String httpResult = elasticClientService.executeGetApi("/_nodes/"+hostName);
        JSONObject jsonObject = JSONObject.parseObject(httpResult);
        return jsonObject;
    }

    @Override
    public EsNodeInfo getOneNodePackaging(Integer nodeId) throws IOException {
        // 从服务获取节点存储实体
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        NodeStoreEntity nodeStoreEntity = elasticNodeStoreService.getByNodeId(currentCluster, nodeId);
        if (nodeStoreEntity == null) {
            log.warn("未找到节点ID为 {} 的节点信息", nodeId);
            return null;
        }
        // 获取节点详细信息
        OneNodeInfoHandler oneNodeInfoHandler = new OneNodeInfoHandler(nodeStoreEntity,elasticClientService);
        EsNodeInfo oneNodeDetail = oneNodeInfoHandler.getOneNodeDetail();
        return oneNodeDetail;
    }


    @Override
    public void deleteNode(Integer nodeId) {
         elasticNodeStoreService.deleteNode(nodeId);
    }

    @Override
    public void updateNode(NodeUpdateReq nodeUpdateReq) {
        elasticNodeStoreService.updateNode(nodeUpdateReq);
    }

    @Override
    public LineChartDataResponse getLineChartData(NodeCharReq nodeCharReq) {
        NodeCharsHandler nodeCharsHandler= new NodeCharsHandler();
        return nodeCharsHandler.getLineChartData(nodeCharReq);
    }

    @Override
    public Integer getClusterNodesCount() throws IOException {
        String result = elasticClientService.executeGetApi(ElasticRestApi.NODES_SIMPLE_LIST.getApiPath());
        JSONArray array = JSONArray.parseArray(result);
        return Integer.valueOf(array.size());
    }


}
