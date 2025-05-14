package com.dipper.monitor.service.elastic.overview.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.cluster.ClusterHealth;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticHealthServiceImpl implements ElasticHealthService {

    private static final Logger log = LoggerFactory.getLogger(ElasticHealthServiceImpl.class);
    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticClusterManagerService elasticClusterManagerService;



    @Override
    public ClusterHealth getHealthData() {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        ElasticClientProxyService elasticClientProxyService = elasticClientService.getInstance(currentCluster);
        String apiPath = ElasticRestApi.CLUSTER_HEALTH.getApiPath();
        Request request = new Request("GET", apiPath);

        try {
            Response response = elasticClientProxyService.performRequest(request);
            String httpResult = EntityUtils.toString(response.getEntity(), "UTF-8");
            log.info("获取返回值信息：{}", httpResult);

            // 使用 FastJSON 解析
            JSONArray jsonArray = JSON.parseArray(httpResult);
            if (jsonArray != null && !jsonArray.isEmpty()) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                ClusterHealth health = new ClusterHealth();

                health.setEpoch(jsonObject.getString("epoch"));
                health.setTimestamp(jsonObject.getString("timestamp"));
                health.setCluster(jsonObject.getString("cluster"));
                health.setStatus(jsonObject.getString("status"));
                health.setNodeTotal(jsonObject.getInteger("node.total"));
                health.setNodeData(jsonObject.getInteger("node.data"));
                health.setShards(jsonObject.getString("shards"));
                health.setPri(jsonObject.getString("pri"));
                health.setRelo(jsonObject.getString("relo"));
                health.setInit(jsonObject.getString("init"));
                health.setUnassign(jsonObject.getString("unassign"));

                // 注意这个特殊字段 unassign.pri
                health.setUnassignPri(jsonObject.getString("unassign.pri"));

                health.setPendingTasks(jsonObject.getString("pending_tasks"));
                health.setMaxTaskWaitTime(jsonObject.getString("max_task_wait_time"));
                health.setActiveShardsPercent(jsonObject.getString("active_shards_percent"));

                return health;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("获取集群信息异常", e);
            return null;
        }
    }
}
