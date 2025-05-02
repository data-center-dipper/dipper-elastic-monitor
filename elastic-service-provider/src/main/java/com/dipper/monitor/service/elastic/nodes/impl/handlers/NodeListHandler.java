package com.dipper.monitor.service.elastic.nodes.impl.handlers;

import com.dipper.common.lib.utils.TelnetUtils;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.db.elastic.NodeStoreEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.nodes.JvmInfoView;
import com.dipper.monitor.entity.elastic.nodes.NodeInfoReq;
import com.dipper.monitor.entity.elastic.nodes.OneNodeTabView;
import com.dipper.monitor.entity.elastic.nodes.OsInfoView;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.entity.elastic.original.nodes.info.EsNodeInfo;
import com.dipper.monitor.entity.elastic.original.nodes.info.nodes.JvmInfo;
import com.dipper.monitor.entity.elastic.original.nodes.info.nodes.OsInfo;
import com.dipper.monitor.entity.elastic.original.nodes.stats.JVM;
import com.dipper.monitor.entity.elastic.original.nodes.stats.Node;
import com.dipper.monitor.entity.elastic.original.nodes.stats.NodeStatsResponse;
import com.dipper.monitor.entity.elastic.original.nodes.stats.OS;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import com.dipper.monitor.service.elastic.nodes.impl.ElasticRealRealNodeServiceImpl;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class NodeListHandler {

    private NodeInfoReq nodeInfoReq;
    private ElasticNodeStoreService elasticNodeStoreService;
    private ElasticClientService elasticClientService;
    private ElasticRealRealNodeServiceImpl elasticRealRealNodeService;

    public NodeListHandler(NodeInfoReq nodeInfoReq,ElasticNodeStoreService elasticNodeStoreService,
                           ElasticRealRealNodeServiceImpl elasticRealRealNodeService) {
        this.nodeInfoReq = nodeInfoReq;
        this.elasticNodeStoreService = elasticNodeStoreService;
        this.elasticClientService = SpringUtil.getBean(ElasticClientService.class);
        this.elasticRealRealNodeService = elasticRealRealNodeService;
    }


    public Tuple2<List<OneNodeTabView>, Integer> nodePageList() throws IOException {
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
        List<EsNodeInfo> esNodes = elasticRealRealNodeService.getEsNodes();
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

        Map<String, EsNodeInfo> nodeMap = esNodes.stream().collect(Collectors.toMap(x -> x.getName(), x -> x));
        Map<String, Node> esNodesStatMap = elasticRealRealNodeService.getEsNodesStatMap();
        Map<String, ElasticNodeDisk> esNodesDiskMap = elasticRealRealNodeService.getEsNodeDiskMap();

        List<OneNodeTabView> oneNodeTabViews = new ArrayList<>();
        for (NodeStoreEntity item:nodeStoreEntities){
            String address = item.getAddress();
            EsNodeInfo esNodeInfo = nodeMap.get(address);
            Node nodeStat = esNodesStatMap.get(address);
            ElasticNodeDisk elasticNodeDisk = esNodesDiskMap.get(address);

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
                JVM jvmStat = nodeStat.getJvm();

                JvmInfoView jvmInfoView = new JvmInfoView();
                jvmInfoView.transToView(jvmInfo,jvmStat);
                oneNodeTabView.setJvmInfoView(jvmInfoView);

                OsInfo osInfo = esNodeInfo.getOsInfo();
                OS osStat = nodeStat.getOs();
                OsInfoView osInfoView = new OsInfoView();
                osInfoView.transToView(osInfo,osStat);
                oneNodeTabView.setOsInfoView(osInfoView);

                oneNodeTabView.setElasticNodeDisk(elasticNodeDisk);

            }
            //如果telnet为false，则设置状态为yellow
            if (telnet) {
                oneNodeTabView.setTelnet("green");
            } else {
                oneNodeTabView.setTelnet("red");
            }

            oneNodeTabViews.add(oneNodeTabView);
        }

        return Tuple2.of(oneNodeTabViews,count);
    }
}
