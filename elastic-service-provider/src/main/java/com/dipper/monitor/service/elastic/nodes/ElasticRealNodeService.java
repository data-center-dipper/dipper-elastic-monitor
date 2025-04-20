package com.dipper.monitor.service.elastic.nodes;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.NodeStoreEntity;
import com.dipper.monitor.entity.elastic.LineChartDataResponse;
import com.dipper.monitor.entity.elastic.nodes.*;
import com.dipper.monitor.entity.elastic.nodes.service.EsNodeFailed;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.EsNodeInfo;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.util.List;

public interface ElasticRealNodeService {
    /**
     * 刷新节点存储信息
     */
    public void refreshNodes();

    /**
     * 获取节点分页列表
     * @param nodeInfoReq
     * @return
     * @throws IOException
     */
    Tuple2<List<OneNodeTabView>, Integer> nodePageList(NodeInfoReq nodeInfoReq) throws IOException;

    /**
     * 获取节点详情信息
     * @param nodeId 节点 ID
     * @return 节点详情响应实体
     */
    NodeDetailView getOneNodeView(Integer nodeId) throws IOException;

    JSONObject getOneNodeOriginal(Integer nodeId)  throws IOException;

    /**
     * 真正去获取节点详情
     */
    EsNodeInfo getOneNodePackaging(Integer nodeId) throws IOException;

    String deleteNode(Integer nodeId);

    void updateNode(NodeUpdateReq nodeUpdateReq);

    LineChartDataResponse getLineChartData(NodeCharReq nodeCharReq);

    Integer getClusterNodesCount() throws IOException;

    List<EsNodeInfo> getEsNodes()  throws IOException ;

    EsNodeFailed getEsNodeFailed() throws IOException;


}
