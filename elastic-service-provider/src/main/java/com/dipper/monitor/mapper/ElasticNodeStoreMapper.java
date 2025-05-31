package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.db.elastic.NodeStoreEntity;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ElasticNodeStoreMapper {
    // 批量插入
    int batchInsert(@Param("list") List<NodeStoreEntity> list);

    // 根据集群编码删除
    int deleteByClusterCode(String clusterCode);

    // 查询集群存储信息
    List<NodeStoreEntity> selectByClusterCode(String clusterCode);

    // 更新存储信息
    int updateByPrimaryKeySelective(NodeStoreEntity entity);

    List<NodeStoreEntity> listByPage(@Param("clusterCode") String clusterCode,
                                     @Param("pageSize") Integer pageSize,
                                     @Param("offset") Integer offset);

    Integer totalNodes(@Param("clusterCode") String clusterCode);

    NodeStoreEntity getNodeByNodeName(@Param("clusterCode") String clusterCode, @Param("brokerName")  String brokerName);

    NodeStoreEntity getBrokerByNodeAndPort(@Param("clusterCode") String clusterCode,
                                           @Param("hostName")  String hostName,
                                           @Param("port")   Integer port);

    /**
     * 根据给定的NodeStoreEntity对象更新t_elastic_node_store表中的记录。
     * @param nodeStoreEntity 包含要更新的信息的实体对象。
     * @return 更新影响的行数。
     */
    int updateBroker(NodeStoreEntity nodeStoreEntity);

    NodeStoreEntity getByNodeId(@Param("nodeId")  Integer nodeId);

    void deleteNode(Integer nodeId);
    
    /**
     * 根据节点名称模糊查询节点列表
     * @param clusterCode 集群编码
     * @param nodeNameLike 节点名称（模糊匹配）
     * @param limit 限制返回数量
     * @return 节点列表
     */
    List<NodeStoreEntity> metricNodes(@Param("clusterCode") String clusterCode, 
                                      @Param("nodeNameLike") String nodeNameLike,
                                      @Param("limit") Integer limit);
}