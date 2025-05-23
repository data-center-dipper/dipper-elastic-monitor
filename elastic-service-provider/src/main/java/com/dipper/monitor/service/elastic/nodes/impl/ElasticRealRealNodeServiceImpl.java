package com.dipper.monitor.service.elastic.nodes.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.db.elastic.NodeStoreEntity;
import com.dipper.monitor.entity.elastic.LineChartDataResponse;
import com.dipper.monitor.entity.elastic.nodes.*;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.nodes.detail.NodeDetailView;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDetail;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.entity.elastic.nodes.service.EsNodeFailed;
import com.dipper.monitor.entity.elastic.original.nodes.info.EsNodeInfo;
import com.dipper.monitor.entity.elastic.original.nodes.stats.Node;
import com.dipper.monitor.entity.elastic.original.nodes.stats.NodeStatsResponse;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import com.dipper.monitor.service.elastic.nodes.impl.handlers.*;
import com.dipper.monitor.service.elastic.nodes.impl.handlers.charts.NodeCharsHandler;
import com.dipper.monitor.service.elastic.nodes.impl.handlers.desc.NodeListHandler;
import com.dipper.monitor.service.elastic.nodes.impl.handlers.desc.NodesInfoHandler;
import com.dipper.monitor.service.elastic.nodes.impl.handlers.desc.OneNodeInfoHandler;
import com.dipper.monitor.utils.Tuple2;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public NodeStatsResponse getEsNodesStat() throws IOException {
        long startTime = System.currentTimeMillis();
        String nodeInfoResult = elasticClientService.executeGetApi(ElasticRestApi.ES_NODES_STAT_MESSAGE.getApiPath());
        JSONObject jsonObject = JSONObject.parseObject(nodeInfoResult);

        NodeStatsResponse nodeStatsResponse = new NodeStatsResponse();
        JSONObject _nodes = jsonObject.getJSONObject("_nodes");
        Integer total = _nodes.getInteger("total");
        Integer successful = _nodes.getInteger("successful");
        Integer failed = _nodes.getInteger("failed");
        nodeStatsResponse.setTotal(total);
        nodeStatsResponse.setSuccessful(successful);
        nodeStatsResponse.setFailed(failed);
        nodeStatsResponse.setCluster_name(jsonObject.getString("cluster_name"));

        Map<String, Node>  esNodes = new HashMap<>();

        JSONObject nodes = jsonObject.getJSONObject("nodes");
        Set<String> strings = nodes.keySet();
        for (String string : strings) {
            JSONObject nodeJson = nodes.getJSONObject(string);
            Node node = nodeJson.toJavaObject(Node.class);
            esNodes.put(string,node);
        }
        nodeStatsResponse.setNodes(esNodes);
        return nodeStatsResponse;
    }

    @Override
    public Map<String, Node> getEsNodesStatMap() throws IOException {
        NodeStatsResponse esNodesStat = getEsNodesStat();
        Map<String, Node> nodes = esNodesStat.getNodes();

        Map<String, Node> resultMap = new HashMap<>();

        for (Map.Entry<String, Node> item: nodes.entrySet()){
            Node node = item.getValue();
            String name = node.getName();
            resultMap.put(name,node);
        }
        return resultMap;
    }


    @Override
    public EsNodeFailed getEsNodeFailed() throws IOException {
        NodeStatsResponse esNodesStat = getEsNodesStat();
        Integer nodesTotal = esNodesStat.getTotal();
        Integer nodesSuccessful = esNodesStat.getSuccessful();
        Integer nodesFailed = esNodesStat.getFailed();

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
        List<ElasticNodeDisk> elasticNodeDisks =  getEsNodeDiskList();
        return elasticNodeDisks.subList(0, Math.min(elasticNodeDisks.size(), 10)); // 返回前十条记录
    }

    public Map<String, ElasticNodeDisk> getEsNodeDiskMap() throws IOException {
        List<ElasticNodeDisk> elasticNodeDisks =  getEsNodeDiskList();
        return elasticNodeDisks.stream().collect(Collectors.toMap(ElasticNodeDisk::getName, Function.identity()));
    }

    @Override
    public JSONObject getOneNodeOriginalStataInfo(Integer nodeId) throws IOException {
        String nodeInfoResult = elasticClientService.executeGetApi(ElasticRestApi.ES_NODES_STAT_MESSAGE.getApiPath());
        JSONObject jsonObject = JSONObject.parseObject(nodeInfoResult);
        return jsonObject;
    }

    @Override
    public List<String> getNodeNameList() throws IOException {
        // 获取集群节点信息
        String nodesInfo = elasticClientService.executeGetApi("/_cat/nodes?format=json");
        log.info("节点信息：\n{}", nodesInfo);

        JSONArray nodesArray = JSONArray.parseArray(nodesInfo);
        List<String> nodeNames = new ArrayList<>();

        for (int i = 0; i < nodesArray.size(); i++) {
            JSONObject nodeJson = nodesArray.getJSONObject(i);
            String nodeName = nodeJson.getString("name");
            if (nodeName != null && !nodeName.isEmpty()) {
                nodeNames.add(nodeName);
            }
        }

        return nodeNames;
    }

    private List<ElasticNodeDisk> getEsNodeDiskList() throws IOException {
        ListHighDiskRiskNodesHandler listHighMemoryRiskNodesHandler = new ListHighDiskRiskNodesHandler(elasticClientService);
        List<ElasticNodeDisk> elasticNodeDisks = listHighMemoryRiskNodesHandler.listHighDiskRiskNodes();
       return elasticNodeDisks;
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
