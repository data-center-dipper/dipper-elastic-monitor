package com.dipper.monitor.service.elastic.client;

import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.Map;

/**
 * todo:不要动
 * Elasticsearch 客户端交互服务接口
 */
public interface ElasticClientService {

     ElasticClientProxyService getInstance(CurrentClusterEntity currentCluster) ;

     public String executeGetApi(String api) throws IOException;


     public String executePostApi(String api, HttpEntity entity) throws IOException;
}
