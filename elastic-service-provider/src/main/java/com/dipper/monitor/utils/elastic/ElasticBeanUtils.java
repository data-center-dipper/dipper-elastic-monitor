package com.dipper.monitor.utils.elastic;

import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.monitor.aware.SpringBeanAwareUtils;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;

public class ElasticBeanUtils {

    private static ElasticClusterManagerService elasticClusterManagerService = null;
    private static ElasticClientService elasticClientService = null;

    /**
     * 获取当前集群信息
     * @return
     */
    public static CurrentClusterEntity getCurrentCluster() {
        if(elasticClusterManagerService == null){
            elasticClusterManagerService = SpringBeanAwareUtils.getBean(ElasticClusterManagerService.class);
        }
        CurrentClusterEntity currentCluster = elasticClusterManagerService.getCurrentCluster();
        return currentCluster;
    }

    public static ElasticClientProxyService getElasticClent(CurrentClusterEntity currentCluster) {
        if(elasticClientService == null){
            elasticClientService = SpringUtil.getBean(ElasticClientService.class);
        }
        ElasticClientProxyService elasticClientProxyService = elasticClientService.getInstance(currentCluster);
        return elasticClientProxyService;
    }

    public static ElasticClientService getElasticClentService(CurrentClusterEntity currentCluster) {
        if(elasticClientService == null){
            elasticClientService = SpringUtil.getBean(ElasticClientService.class);
        }
        return elasticClientService;
    }
}
