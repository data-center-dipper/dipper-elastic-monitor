package com.dipper.monitor.service.elastic.nodes.impl;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.db.elastic.NodeStoreEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.nodes.NodeUpdateReq;
import com.dipper.monitor.mapper.ElasticNodeStoreMapper;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ElasticNodeStoreServiceImpl implements ElasticNodeStoreService {

    @Autowired
    private ElasticNodeStoreMapper elasticNodeStoreMapper;


    @Override
    public void saveOrUpdateBrokerStore(String clusterCode, List<NodeStoreEntity> nodeStoreEntities) {
        // 如果传入的节点列表为空，则直接返回
        if (nodeStoreEntities == null || nodeStoreEntities.isEmpty()) {
            return;
        }

        // 从数据库中获取当前集群下的所有节点存储实体
        List<NodeStoreEntity> nodeStoreEntitiesInDb = elasticNodeStoreMapper.selectByClusterCode(clusterCode);

        // 存储需要新增的节点实体
        List<NodeStoreEntity> storeList = new ArrayList<>();

        // 遍历传入的节点列表，判断是否已存在
        for (NodeStoreEntity nodeStoreEntity : nodeStoreEntities) {
            // 使用anyMatch来检查是否存在相同address的记录
            boolean exists = nodeStoreEntitiesInDb.stream()
                    .anyMatch(item -> item.getAddress().equals(nodeStoreEntity.getAddress()));

            // 如果不存在，则添加到待插入列表
            if (!exists) {
                nodeStoreEntity.setClusterCode(clusterCode); // 确保clusterCode被设置
                storeList.add(nodeStoreEntity);
            }
        }

        // 如果有新的节点需要插入，则进行批量插入
        if (!CollectionUtils.isEmpty(storeList)) {
            elasticNodeStoreMapper.batchInsert(storeList);
        }
    }

    @Override
    public List<NodeStoreEntity> listByPage(String clusterCode, Integer pageNum, Integer pageSize) {
        Integer offset = (pageNum - 1) * pageSize; // 计算offset
        return elasticNodeStoreMapper.listByPage(clusterCode, pageSize, offset);
    }

    @Override
    public Integer totalNodes(String clusterCode) {
        return elasticNodeStoreMapper.totalNodes(clusterCode);
    }

    @Override
    public NodeStoreEntity getByNodeName(String clusterCode, String brokerName) {
        return elasticNodeStoreMapper.getNodeByNodeName(clusterCode, brokerName);
    }

    @Override
    public NodeStoreEntity getBrokerByNodeAndPort(String clusterCode, String hostName, Integer port) {
        return elasticNodeStoreMapper.getBrokerByNodeAndPort(clusterCode, hostName, port);
    }

    @Override
    public void deleteNode(Integer nodeId) {
         elasticNodeStoreMapper.deleteNode(nodeId);
    }

    @Override
    public NodeStoreEntity getByNodeId(CurrentClusterEntity currentCluster,Integer nodeId) {
        String clusterCode = currentCluster.getClusterCode();
        return elasticNodeStoreMapper.getByNodeId(clusterCode,nodeId);
    }

    @Override
    public void updateNode(NodeUpdateReq nodeUpdateReq) {
        NodeStoreEntity nodeStoreEntity = new NodeStoreEntity();
        BeanUtils.copyProperties(nodeUpdateReq,nodeStoreEntity);
        elasticNodeStoreMapper.updateBroker(nodeStoreEntity);
    }

    @Override
    public List<String> metricNodes(String nodeNameLike) {
        // 获取当前集群编码
        CurrentClusterEntity currentCluster = getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        
        // 查询节点列表
        List<NodeStoreEntity> nodeList;
        if (StringUtils.isBlank(nodeNameLike)) {
            // 如果未提供搜索关键词，则获取前10个节点
            nodeList = listByPage(clusterCode, 1, 10);
        } else {
            // 否则模糊查询前10个匹配的节点
            nodeList = elasticNodeStoreMapper.metricNodes(clusterCode, nodeNameLike, 10);
        }
        
        // 转换为节点名称列表
        return nodeList.stream()
                .map(node -> {
                    // 构建节点信息，格式："id:hostName:hostIp"
                    return node.getId() + ":" + node.getHostName() + ":" + node.getHostIp();
                })
                .collect(Collectors.toList());
    }

    private CurrentClusterEntity getCurrentCluster() {
        ElasticClusterManagerService elasticClusterManagerService = SpringUtil.getBean(ElasticClusterManagerService.class);
        CurrentClusterEntity currentCluster = elasticClusterManagerService.getCurrentCluster();
        return currentCluster;
    }


    private List<NodeStoreEntity> buildStoreEntities(String addresses, String kafkaJmxAddress,
                                                     String clusterCode, Integer networkRate) {
        Map<String, Integer> jmxPortMap = getJmxPortMap(kafkaJmxAddress);

        String[] addressList = addresses.split(",");
        List<NodeStoreEntity> result = new ArrayList<>();

        for (String addr : addressList) {
            try {
                String[] split = addr.split(":");
                if (split.length != 2) {
                    throw new IllegalArgumentException("地址格式错误: " + addr);
                }
                String hostName = split[0].trim();
                Integer jmxPort = jmxPortMap.getOrDefault(hostName, -1);

                NodeStoreEntity entity = new NodeStoreEntity();
                entity.setClusterCode(clusterCode);
                entity.setHostIp(resolveHostIp(split[0].trim())); // 实际需要DNS解析
                entity.setHostPort(Integer.parseInt(split[1]));


                result.add(entity);
            } catch (Exception e) {
                log.error("Broker地址解析失败: {}", addr, e);
            }
        }
        return result;
    }

    private Map<String, Integer> getJmxPortMap(String addresses) {
        if (StringUtils.isBlank(addresses)) {
            return Collections.emptyMap();
        }
        String[] addressList = addresses.split(",");
        Map<String, Integer> resultMap = new HashMap<>();
        for (String item : addressList) {
            try {
                String[] split = item.split(":");
                if (split.length != 2) {
                    log.warn("地址格式错误: " + item);
                    continue;
                }
                String host = split[0].trim();
                Integer port = Integer.parseInt(split[1].trim());
                resultMap.put(host, port);
            } catch (Exception e) {
                log.error("解析异常", e);
            }
        }
        return resultMap;
    }

    private String resolveHostIp(String hostname) {
        // 实现实际的DNS解析逻辑
        return hostname; // 示例返回
    }
}