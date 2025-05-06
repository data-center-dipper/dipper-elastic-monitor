package com.dipper.monitor.service.elastic.overview.impl;

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

            ObjectMapper objectMapper = new ObjectMapper();
            // 反序列化JSON数组为List<ClusterHealth>
            List<ClusterHealth> clusterHealthList = objectMapper.readValue(httpResult, new TypeReference<List<ClusterHealth>>(){});

            // 假设我们只关心第一个元素
            if (clusterHealthList != null && !clusterHealthList.isEmpty()) {
                return clusterHealthList.get(0);
            } else {
                return null; // 或者根据需求抛出异常、返回默认实例等
            }
        } catch (Exception e) {
            log.error("获取集群信息异常", e);
            return null; // 或者根据需要返回一个默认的ClusterHealth实例
        }
    }
}
