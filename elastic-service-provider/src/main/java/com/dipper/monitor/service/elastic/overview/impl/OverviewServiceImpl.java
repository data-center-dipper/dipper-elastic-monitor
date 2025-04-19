package com.dipper.monitor.service.elastic.overview.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.cluster.ClusterHealth;
import com.dipper.monitor.entity.elastic.cluster.ClusterStatsParse;
import com.dipper.monitor.entity.elastic.cluster.ClusterStatusView;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.life.LifecyclePoliciesService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeService;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.service.elastic.overview.OverviewService;
import com.dipper.monitor.service.elastic.overview.impl.service.ClusterErrorService;
import com.dipper.monitor.service.elastic.overview.impl.service.ClusterStatusService;
import com.dipper.monitor.utils.ListUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private LifecyclePoliciesService lifecyclePoliciesService;


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



    @Override
    public List<JSONObject>  getLifeCycleError(PageReq pageReq) {
        // 获取原始的生命周期列表
        List<JSONObject> lifeCycleList = lifecyclePoliciesService.getLifeCycleList();
        if(CollectionUtils.isEmpty(lifeCycleList)){
            return Collections.emptyList();
        }
        // 进行分页处理
        Integer pageNum = pageReq.getPageNum();
        Integer pageSize = pageReq.getPageSize();
        List<List<JSONObject>> lists = ListUtils.splitListBySize(lifeCycleList, pageSize);
        List<JSONObject> result = lists.get(pageNum - 1);
        // 转换为 Map
        return result;
    }

}
