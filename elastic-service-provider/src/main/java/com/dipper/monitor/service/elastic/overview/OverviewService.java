package com.dipper.monitor.service.elastic.overview;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.cluster.ClusterStatusView;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDetail;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;

import java.io.IOException;
import java.util.List;

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

    /**
     * 获取分片错误信息
     * @param pageReq
     * @return
     */
    List<JSONObject> getShardError(PageReq pageReq) throws IOException;

    /**
     * 获取集群健康状态
     * @return
     */
    String checkLifeCycleError() throws IOException;

    /**
     * 修复生命周期错误
     * @return
     * @throws IOException
     */
    String repairLifeCycleError() throws IOException;


    /**
     * 检查分片错误
     * @return
     */
    String checkShardError() throws Exception;

    /**
     * 修复分片错误
     * @return
     */
    String repairShardError() throws Exception;


    List<ElasticNodeDetail> nodeMemoryTop10() throws IOException;

    List<ElasticNodeDisk> nodeDiskTop10() throws IOException;
}
