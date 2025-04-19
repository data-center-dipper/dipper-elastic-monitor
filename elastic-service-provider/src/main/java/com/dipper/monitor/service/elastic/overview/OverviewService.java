package com.dipper.monitor.service.elastic.overview;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.cluster.ClusterStatusView;

import java.util.List;
import java.util.Map;

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
     * 获取生命周期错误信息
     * @return 生命周期错误信息
     */
    List<JSONObject>  getLifeCycleError(PageReq pageReq);
}
