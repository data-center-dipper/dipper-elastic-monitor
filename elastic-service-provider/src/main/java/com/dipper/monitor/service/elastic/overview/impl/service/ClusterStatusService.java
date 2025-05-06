package com.dipper.monitor.service.elastic.overview.impl.service;

import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.cluster.ClusterStatsParse;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;

@Slf4j
public class ClusterStatusService {

    public ClusterStatsParse getClusterStatus() {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        ElasticClientProxyService elasticClient = ElasticBeanUtils.getElasticClent(currentCluster);

        Request request = new Request("GET", "/_cluster/stats?format=json");
        try {
            Response response = elasticClient.performRequest(request);
            String httpResult = EntityUtils.toString(response.getEntity(), "UTF-8");
            log.info("获取返回值信息：{}", httpResult);

            ObjectMapper objectMapper = new ObjectMapper();
            ClusterStatsParse clusterStatsResponse = objectMapper.readValue(httpResult, ClusterStatsParse.class);

            return clusterStatsResponse;

        } catch (Exception e) {
            log.error("获取集群状态异常", e);
        }
        return null;
    }
}
