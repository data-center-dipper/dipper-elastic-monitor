package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.db.elastic.ElasticClusterEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ElasticClusterManagerMapper {

    ElasticClusterEntity getClusterByCode(@Param("clusterCode") String clusterCode);

    void save(ElasticClusterEntity elasticClusterEntity);

    void update(ElasticClusterEntity elasticClusterEntity);

    void deleteCluster(@Param("clusterCode") String clusterCode);

    // 获取集群的节点
    List<ElasticClusterEntity> getAllClusterList();

    void setDefaultCluster(@Param("clusterCode") String clusterCode);

    void clearDefaultCluster(@Param("clusterCode") String clusterCode);

    void clearCurrentCluster(@Param("clusterCode") String clusterCode);

    void setCurrentCluster(@Param("clusterCode") String clusterCode,@Param("currentEnable") Boolean currentEnable);

    ElasticClusterEntity getCurrentCluster();

    void clearMonitoringPolicy(@Param("clusterCode") String clusterCode);

    void updateClusterVersion(@Param("clusterCode") String clusterCode,
                              @Param("clusterVersion")  String clusterVersion);
}
