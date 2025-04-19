package com.dipper.monitor.service.elastic.client;

import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;

import java.util.Map;

/**
 * todo:不要动
 * Elasticsearch 客户端交互服务接口
 */
public interface ElasticClientService {

     ElasticClientProxyService getInstance(CurrentClusterEntity currentCluster) ;

}
