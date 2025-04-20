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

    NodeStoreEntity getBrokerByNodeName(@Param("clusterCode") String clusterCode, @Param("brokerName")  String brokerName);

    NodeStoreEntity getBrokerByNodeAndPort(@Param("clusterCode") String clusterCode,
                                           @Param("hostName")  String hostName,
                                           @Param("port")   Integer port);

    void updateBroker(NodeStoreEntity brokerByNodeAndPort);
}