package com.dipper.monitor.service.elastic.nodes.impl.service;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.db.elastic.NodeStoreEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.nodes.NodeDetailView;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.EsNodeInfo;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.JvmInfo;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.OsInfo;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.PathInfo;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import com.dipper.monitor.service.elastic.nodes.impl.ElasticRealRealNodeServiceImpl;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class NodeDescHandler {

    private Integer nodeId;
    private ElasticNodeStoreService elasticNodeStoreService;
    private ElasticClientService elasticClientService;
    private ElasticRealRealNodeServiceImpl elasticRealRealNodeService;
    private CurrentClusterEntity currentCluster;

    public NodeDescHandler(Integer nodeId,ElasticNodeStoreService elasticNodeStoreService,
                           ElasticRealRealNodeServiceImpl elasticRealRealNodeService) {
        this.nodeId = nodeId;
        this.elasticNodeStoreService = elasticNodeStoreService;
        this.elasticClientService = SpringUtil.getBean(ElasticClientService.class);
        this.currentCluster = ElasticBeanUtils.getCurrentCluster();
        this.elasticRealRealNodeService = elasticRealRealNodeService;
    }

    public NodeDetailView getNodeDetail() throws IOException {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        NodeStoreEntity nodeStoreEntity = elasticNodeStoreService.getByNodeId(currentCluster, nodeId);
        if (nodeStoreEntity == null) {
            log.warn("未找到节点ID为 {} 的节点信息", nodeId);
            return null;
        }

        EsNodeInfo esNodeInfo = elasticRealRealNodeService.getOneNodePackaging(nodeId);

        if (esNodeInfo == null) {
            log.warn("未能获取节点ID为 {} 的详细信息", nodeId);
            return null;
        }

        // 创建并填充NodeDetailView对象
        NodeDetailView nodeDetailView = new NodeDetailView();
        nodeDetailView.setId(nodeStoreEntity.getId());
        nodeDetailView.setClusterCode(nodeStoreEntity.getClusterCode());
        nodeDetailView.setHostName(nodeStoreEntity.getHostName());
        nodeDetailView.setHostIp(nodeStoreEntity.getHostIp());
        nodeDetailView.setHostPort(nodeStoreEntity.getHostPort());
        nodeDetailView.setAddress(nodeStoreEntity.getAddress());

        // 设置JVM信息
        JvmInfo jvmInfo = esNodeInfo.getJvmInfo();
        if (jvmInfo != null) {
            nodeDetailView.setJvmInfo(jvmInfo);
        }

        // 设置操作系统信息
        OsInfo osInfo = esNodeInfo.getOsInfo();
        if (osInfo != null) {
            nodeDetailView.setOsInfo(osInfo);
        }

        // 设置路径信息
        PathInfo pathInfo = esNodeInfo.getPathInfo();
        if (pathInfo != null) {
            nodeDetailView.setPathInfo(pathInfo);
        }

        // 设置是否为主节点
        nodeDetailView.setMaster(esNodeInfo.isMaster());

        return nodeDetailView;
    }
}
