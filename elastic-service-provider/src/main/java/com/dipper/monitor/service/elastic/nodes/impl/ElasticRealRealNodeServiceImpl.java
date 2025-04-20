package com.dipper.monitor.service.elastic.nodes.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.common.lib.utils.TelnetUtils;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.db.elastic.NodeStoreEntity;
import com.dipper.monitor.entity.elastic.LineChartDataResponse;
import com.dipper.monitor.entity.elastic.nodes.*;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.nodes.service.EsNodeFailed;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.EsNodeInfo;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.JvmInfo;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import com.dipper.monitor.service.elastic.nodes.impl.service.NodeInfoService;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        ElasticClusterManagerService managerService = SpringUtil.getBean(ElasticClusterManagerService.class);
        CurrentClusterEntity currentCluster = managerService.getCurrentCluster();

        log.info("准备刷新节点数据");
        ElasticClientProxyService clientProxyService = elasticClientService.getInstance(currentCluster);
        NodeInfoService nodeInfoService = new NodeInfoService(clientProxyService);
        List<EsNodeInfo> esNodes = nodeInfoService.getEsNodes();
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
    public Tuple2<List<OneNodeTabView>, Integer> nodePageList(NodeInfoReq nodeInfoReq) throws IOException {
        ElasticClusterManagerService managerService = SpringUtil.getBean(ElasticClusterManagerService.class);
        CurrentClusterEntity currentCluster = managerService.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();

        Integer pageNum = nodeInfoReq.getPageNum();
        Integer pageSize = nodeInfoReq.getPageSize();

        Integer count = elasticNodeStoreService.totalNodes(clusterCode);
        List<NodeStoreEntity> nodeStoreEntities = elasticNodeStoreService.listByPage(clusterCode, pageNum, pageSize);
        if(nodeStoreEntities == null || nodeStoreEntities.isEmpty()){
            return Tuple2.of(new ArrayList<>(),count);
        }

        // 获取在线的节点
        ElasticClientProxyService clientProxyService = elasticClientService.getInstance(currentCluster);
        List<EsNodeInfo> esNodes = getEsNodes();
        if(esNodes == null || esNodes.isEmpty()){
            List<OneNodeTabView> oneNodeTabViews = new ArrayList<>();
            // todo: 没有在线的节点，那么默认所有节点都是处于离线状态
            for (NodeStoreEntity item:nodeStoreEntities) {
                OneNodeTabView oneNodeTabView = new OneNodeTabView();
                oneNodeTabView.setAddress(item.getAddress());
                oneNodeTabView.setHostName(item.getHostName());
                oneNodeTabView.setHostPort(item.getHostPort());
                oneNodeTabView.setStatus("red");
                oneNodeTabView.setTelnet("red");
                oneNodeTabViews.add(oneNodeTabView);
            }
            return Tuple2.of(oneNodeTabViews,count);
        }

        Map<String, EsNodeInfo> nodeMap = esNodes.stream().collect(Collectors.toMap(x -> x.getHost(), x -> x));

        List<OneNodeTabView> oneNodeTabViews = new ArrayList<>();
        for (NodeStoreEntity item:nodeStoreEntities){
            String address = item.getAddress();
            EsNodeInfo esNodeInfo = nodeMap.get(address);

            OneNodeTabView oneNodeTabView = new OneNodeTabView();
            oneNodeTabView.setAddress(item.getAddress());
            oneNodeTabView.setHostName(item.getHostName());
            oneNodeTabView.setHostPort(item.getHostPort());

            boolean telnet = TelnetUtils.telnet(item.getHostName(), item.getHostPort(), 3000);

            if(esNodeInfo == null){
                oneNodeTabView.setStatus("red");
            }else {
                BeanUtils.copyProperties(item, oneNodeTabView);
                oneNodeTabView.setStatus("green");

                JvmInfo jvmInfo = esNodeInfo.getJvmInfo();
                JvmInfoView jvmInfoView = new JvmInfoView();
                jvmInfoView.transToView(jvmInfo);
                oneNodeTabView.setJvmInfoView(jvmInfoView);
            }
            //如果telnet为false，则设置状态为yellow
            if (telnet) {
                oneNodeTabView.setTelnet("green");
            } else {
                oneNodeTabView.setTelnet("red");
            }

            oneNodeTabViews.add(oneNodeTabView);
        }

        return Tuple2.of(new ArrayList<>(),count);
    }


    @Override
    public NodeDetailView getNodeDetail(Integer nodeId) {
        NodeDetailView response = new NodeDetailView();
//        Map<String, Object> nodeDetail = getNodeDetail(nodeId);
//        response.setBasicInfo((Map<String, Object>) nodeDetail.get("basicInfo"));
//        response.setJvmInfo((String) nodeDetail.get("jvmInfo"));
//        response.setDiskUsage((String) nodeDetail.get("diskUsage"));
//        response.setBarrelEffect((String) nodeDetail.get("barrelEffect"));
//        response.setThreadInfo((String) nodeDetail.get("threadInfo"));
//        response.setRawInfo((String) nodeDetail.get("rawInfo"));
        return response;
    }


    @Override
    public String deleteNode(Integer nodeId) {
        boolean result = elasticNodeStoreService.deleteNode(nodeId);
        return result ? "删除成功" : "删除失败";
    }

    @Override
    public void updateNode(NodeUpdateReq nodeUpdateReq) {

    }

    @Override
    public LineChartDataResponse getLineChartData(NodeCharReq nodeCharReq) {
        return null;
    }

    @Override
    public Integer getClusterNodesCount() throws IOException {
        String result = elasticClientService.executeGetApi(ElasticRestApi.NODES_SIMPLE_LIST.getApiPath());
        JSONArray array = JSONArray.parseArray(result);
        return Integer.valueOf(array.size());
    }


}
