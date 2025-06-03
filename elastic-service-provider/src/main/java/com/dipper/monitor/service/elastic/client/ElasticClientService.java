package com.dipper.monitor.service.elastic.client;

import com.alibaba.fastjson.JSONObject;
import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadPoolStat;
import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * todo:不要动
 * Elasticsearch 客户端交互服务接口
 */
public interface ElasticClientService {

     ElasticClientProxyService getInstance(CurrentClusterEntity currentCluster) ;

     Request buildRequest(String method, String endPoint, Map<String, String> paramMap,
                                 HttpEntity entity, Header... headers);

     public String executeGetApi(String api) throws IOException;

     String executePostApi(String api, HttpEntity entity) throws IOException;
     Response executePostApiReturnResponse(String api, HttpEntity entity) throws IOException;


     String executePutApi(String api, HttpEntity nStringEntity);
     Response executePutApiReturnResponse(String api, HttpEntity nStringEntity);
     Response executePutApiReturnResponseEx(String api, HttpEntity nStringEntity) throws Exception;

     boolean executeHeadApi(String api);

     String executeDeleteApi(String apiUrl, HttpEntity entity) throws IOException;
     Response executeDeleteApiReturnResponse(String apiUrl, HttpEntity entity) throws IOException;

}
