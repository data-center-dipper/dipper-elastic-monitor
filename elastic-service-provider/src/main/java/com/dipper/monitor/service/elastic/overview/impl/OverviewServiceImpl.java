package com.dipper.monitor.service.elastic.overview.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.cluster.ClusterHealth;
import com.dipper.monitor.entity.elastic.cluster.ClusterStatsParse;
import com.dipper.monitor.entity.elastic.cluster.ClusterStatusView;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDetail;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.life.LifecyclePoliciesService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.service.elastic.overview.OverviewService;
import com.dipper.monitor.service.elastic.overview.impl.service.ClusterStatusService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import com.dipper.monitor.utils.ListUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 集群预览服务接口实现类
 */
@Service
public class OverviewServiceImpl implements OverviewService {

    @Autowired
    private  ElasticClientService elasticClientService;
    @Autowired
    private ElasticRealNodeService elasticRealNodeService;
    @Autowired
    private  ElasticHealthService elasticHealthService;
    @Autowired
    private LifecyclePoliciesService lifecyclePoliciesService;
    @Autowired
    private ElasticShardService elasticShardService;


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
    public List<EsLifeCycleManagement>  getLifeCycleError(PageReq pageReq) {
        // 获取原始的生命周期列表
        List<EsLifeCycleManagement> lifeCycleList = lifecyclePoliciesService.getLifeCycleList();
        if(CollectionUtils.isEmpty(lifeCycleList)){
            return Collections.emptyList();
        }
        // 进行分页处理
        Integer pageNum = pageReq.getPageNum();
        Integer pageSize = pageReq.getPageSize();
        List<List<EsLifeCycleManagement>> lists = ListUtils.splitListBySize(lifeCycleList, pageSize);
        List<EsLifeCycleManagement> result = lists.get(pageNum - 1);
        // 转换为 Map
        return result;
    }

    @Override
    public List<JSONObject> getShardError(PageReq pageReq) throws IOException {
        // 获取原始的生命周期列表
        List<JSONObject>  lifeCycleList = elasticShardService.getShardError();
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

    @Override
    public String checkLifeCycleError() throws IOException {
        String lifeCycleList = lifecyclePoliciesService.checkLifeCycleError();
        return  lifeCycleList;
    }

    @Override
    public String repairLifeCycleError() throws IOException {
        String lifeCycleList = lifecyclePoliciesService.repairLifeCycleError();
        return  lifeCycleList;
    }

    @Override
    public String checkShardError() throws Exception {
        String lifeCycleList = elasticShardService.checkShardError();
        return  lifeCycleList;
    }

    @Override
    public String repairShardError() throws Exception {
        String lifeCycleList = elasticShardService.repairShardError();
        return  lifeCycleList;
    }

    @Override
    public List<ElasticNodeDetail> nodeMemoryTop10() throws IOException {
        return elasticRealNodeService.nodeMemoryTop10();
    }

    @Override
    public List<ElasticNodeDisk> nodeDiskTop10() throws IOException {
       return  elasticRealNodeService.nodeDiskTop10();
    }

}
