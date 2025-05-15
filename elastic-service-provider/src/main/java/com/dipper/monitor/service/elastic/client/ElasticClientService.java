package com.dipper.monitor.service.elastic.client;

import com.alibaba.fastjson.JSONObject;
import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.IOException;
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

     boolean executeHeadApi(String api);

     public String executeDeleteApi(String apiUrl, HttpEntity entity) throws IOException;

    
    /**
     * 创建索引
     * @param indexName 索引名称
     * @param templateJson 模板JSON
     * @return 操作结果
     * @throws IOException IO异常
     */
    String createIndex(String indexName, JSONObject templateJson) throws IOException;
}
