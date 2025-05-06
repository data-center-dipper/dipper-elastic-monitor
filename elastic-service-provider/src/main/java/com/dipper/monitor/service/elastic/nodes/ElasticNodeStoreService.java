package com.dipper.monitor.service.elastic.nodes;


import com.dipper.monitor.entity.db.elastic.NodeStoreEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.nodes.NodeUpdateReq;

import java.util.List;

/**
 * 主要是kafka节点列表存储
 */
public interface ElasticNodeStoreService {
    /**
     * 保存或更新kafka节点列表
     */
    void saveOrUpdateBrokerStore(String clusterCode,List<NodeStoreEntity> nodeStoreEntities);

    List<NodeStoreEntity> listByPage(String clusterCode, Integer pageNum, Integer pageSize);

    Integer totalNodes(String clusterCode);

    /**
     * 获取指定节点的信息
     */
    NodeStoreEntity getByNodeName(String clusterCode, String brokerName);



    NodeStoreEntity getBrokerByNodeAndPort(String clusterCode, String hostName, Integer port);

    void deleteNode(Integer nodeId);

    NodeStoreEntity getByNodeId(CurrentClusterEntity currentCluster,Integer nodeId);

    void updateNode(NodeUpdateReq nodeUpdateReq);
}
