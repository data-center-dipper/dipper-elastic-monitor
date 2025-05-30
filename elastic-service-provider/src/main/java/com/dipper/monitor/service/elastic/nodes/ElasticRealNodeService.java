package com.dipper.monitor.service.elastic.nodes;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.nodes.*;
import com.dipper.monitor.entity.elastic.nodes.detail.NodeDetailView;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDetail;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.entity.elastic.nodes.service.EsNodeFailed;
import com.dipper.monitor.entity.elastic.original.nodes.info.EsNodeInfo;
import com.dipper.monitor.entity.elastic.original.nodes.stats.Node;
import com.dipper.monitor.entity.elastic.original.nodes.stats.NodeStatsResponse;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    void deleteNode(Integer nodeId);

    void updateNode(NodeUpdateReq nodeUpdateReq);


    Integer getClusterNodesCount() throws IOException;

    /**
     * 获取节点通用信息
     * @return
     * @throws IOException
     */
    List<EsNodeInfo> getEsNodes()  throws IOException ;

    /**
     * 获取节点统计信息
     * @return
     */
    NodeStatsResponse getEsNodesStat() throws IOException;

    /**
     * 转成map结构
     * @return
     */
    Map<String, Node> getEsNodesStatMap() throws IOException;

    EsNodeFailed getEsNodeFailed() throws IOException;


    List<ElasticNodeDetail>  nodeMemoryTop10() throws IOException;

    List<ElasticNodeDisk>  nodeDiskTop10() throws IOException;

    Map<String, ElasticNodeDisk> getEsNodeDiskMap() throws IOException;

    /**
     * 获取节点原始统计信息
     * @param nodeId
     * @return
     */
    JSONObject getOneNodeOriginalStataInfo(Integer nodeId) throws IOException;

    List<String> getNodeNameList() throws IOException;
}
