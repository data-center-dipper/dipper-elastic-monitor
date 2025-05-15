package com.dipper.monitor.service.elastic.overview;

import com.dipper.monitor.entity.elastic.cluster.ClusterHealth;

import java.util.Map;

public interface ElasticHealthService {
    /**
     * 获取集群健康信息
     * @return
     */
    ClusterHealth getHealthData();

}
