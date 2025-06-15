package com.dipper.monitor.service.elastic.cluster;


import com.dipper.monitor.entity.db.elastic.ElasticClusterEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterReq;
import com.dipper.monitor.entity.elastic.cluster.ElasticClusterRegisterReq;
import com.dipper.monitor.entity.elastic.cluster.ElasticClusterView;


import java.util.List;

public interface ElasticClusterManagerService {

    void registerCluster(ElasticClusterRegisterReq clusterRegisterReq);

    void updateCluster(ElasticClusterRegisterReq clusterRegisterReq);

    void deleteCluster(String clusterCode);

    void setDefaultCluster(String clusterCode);

    void setCurrentCluster(CurrentClusterReq currentClusterReq);

    CurrentClusterEntity getCurrentCluster();

    // 获取当前要展示的集群
    ElasticClusterEntity getCurrentClusterDetail(String clusterCode);

    /**
     * 获取集群列表
     * @return
     */
    List<ElasticClusterView> getAllCluster();


    /**
     * 获取集群版本信息
     * @return
     */
    void updateClusterVersion();

    ElasticClusterEntity getClusterById(String id);
}
