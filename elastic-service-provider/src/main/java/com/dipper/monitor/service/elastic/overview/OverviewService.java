package com.dipper.monitor.service.elastic.overview;

import com.dipper.monitor.entity.elastic.cluster.ClusterStatusView;

/**
 * 集群预览服务接口
 */
public interface OverviewService {
    /**
     * 获取集群状态信息
     * @return 集群状态响应实体
     */
    ClusterStatusView getClusterStatus();

    /**
     * 获取集群是否存在异常
     */
    String clusterError();
}
