package com.dipper.monitor.service.elastic.overview.impl;

import com.dipper.monitor.entity.elastic.cluster.ClusterHealth;
import com.dipper.monitor.entity.elastic.cluster.ClusterStatsParse;
import com.dipper.monitor.entity.elastic.cluster.ClusterStatusView;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeService;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.service.elastic.overview.OverviewService;
import com.dipper.monitor.service.elastic.overview.impl.service.ClusterErrorService;
import com.dipper.monitor.service.elastic.overview.impl.service.ClusterStatusService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 集群预览服务接口实现类
 */
@Service
public class OverviewServiceImpl implements OverviewService {

    @Autowired
    private  ElasticClientService elasticClientService;
    @Autowired
    private  ElasticNodeService elasticNodeService;
    @Autowired
    private  ElasticHealthService elasticHealthService;


    @Override
    public ClusterStatusView getClusterStatus() {
        ClusterStatusView response = new ClusterStatusView();
        // 调用 ElasticClientService 获取数据
        ClusterHealth healthData = elasticHealthService.getHealthData();
        BeanUtils.copyProperties(healthData, response);

        ClusterStatusService clusterStatusService = new ClusterStatusService();
        ClusterStatsParse clusterStatus = clusterStatusService.getClusterStatus();
        response.setClusterStatus(clusterStatus);

        return response;
    }

    /**
     * 获取集群信息是否异常
     */
    @Override
    public String clusterError() {
        ClusterErrorService clusterErrorService = new ClusterErrorService();
        String clusterError = clusterErrorService.getClusterError();
        return clusterError;
    }


}
