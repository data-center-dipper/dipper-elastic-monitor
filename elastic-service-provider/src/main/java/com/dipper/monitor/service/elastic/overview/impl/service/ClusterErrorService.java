package com.dipper.monitor.service.elastic.overview.impl.service;

import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;

public class ClusterErrorService {

    public void getClusterError() {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        ElasticClientProxyService elasticClent = ElasticBeanUtils.getElasticClent(currentCluster);

        // 获取集群 生命周期是否存在异常
        // 获取集群 分片是否存在异常

    }
}
