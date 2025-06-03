package com.dipper.monitor.service.elastic.client.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.client.proxy.config.ElasticsearchBaseProxyConfig;
import com.dipper.client.proxy.config.PluginConfig;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.config.plugins.PluginsClientLoader;
import com.dipper.monitor.config.plugins.PluginsConfigUtils;
import com.dipper.monitor.constants.PluginConstants;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import com.dipper.monitor.utils.plugins.PluginConfigUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.apache.http.message.BasicHeader;
import com.dipper.client.proxy.params.elasticsearch.RequestOptions ;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class ElasticClientServiceImpl implements ElasticClientService {

    private static final Cache<String, ElasticClientProxyService> clientCache = CacheBuilder.newBuilder()
            .maximumSize(50L)
            .expireAfterAccess(10L, TimeUnit.MINUTES)
            .concurrencyLevel(7)
            .build();

    private Header[] commonHeaders = new Header[1];


    @PostConstruct
    public synchronized void postInit() {

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Content-Type", "application/json"));

        this.commonHeaders = headers.toArray(this.commonHeaders);
    }

    @Override
    public ElasticClientProxyService getInstance(CurrentClusterEntity currentCluster) {
        String clusterCode = currentCluster.getClusterCode();
        ElasticClientProxyService elasticClientProxyService = clientCache.getIfPresent(clusterCode);
        if (elasticClientProxyService == null) {
            log.info("缓存中没有找到对应集群的client，重新获取");
            elasticClientProxyService = createClient(currentCluster);
        }
        if(elasticClientProxyService != null){
            clientCache.put(clusterCode, elasticClientProxyService);
        }
        return elasticClientProxyService;
    }

    private ElasticClientProxyService createClient(CurrentClusterEntity currentCluster) {

        Properties properties = PluginsConfigUtils.getPluginConfig(PluginConstants.ELASTICSEARCH.getPluginName());
        if (properties == null) {
            log.error("未找到插件配置");
        }
        String address = currentCluster.getAddress();
        properties.put(ElasticsearchBaseProxyConfig.ELASTICSEARCH_URL, address);
        log.info("创建admin配置:{}",properties);

        PluginConfig pluginConfig = PluginConfigUtils.getElasticConfig(properties);
        ElasticsearchBaseProxyConfig elasticsearchBaseProxyConfig = new ElasticsearchBaseProxyConfig(properties);

        ElasticClientProxyService elasticClientProxyService = PluginsClientLoader.loadComponentClient(pluginConfig, ElasticClientProxyService.class, elasticsearchBaseProxyConfig);
        return elasticClientProxyService;
    }


    public String executeGetApi(String api) throws IOException {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        ElasticClientProxyService elasticClientProxyService = getInstance(currentCluster);
        Response response = elasticClientProxyService.performRequest(new Request(RequestMethod.GET.name(), api));
        String httpResult = EntityUtils.toString(response.getEntity(), "UTF-8");
        return httpResult;
    }



    @Override
    public String executePostApi(String api, HttpEntity entity) throws IOException {
        Response response = executePostApiReturnResponse(api, entity);
        String responseData = EntityUtils.toString(response.getEntity());
        return responseData;
    }

    @Override
    public Response executePostApiReturnResponse(String api, HttpEntity entity) throws IOException {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        ElasticClientProxyService elasticClientProxyService = getInstance(currentCluster);
        Response response = null;
        if (entity != null) {
            Request request = buildRequest(RequestMethod.POST.name(), api,
                    Collections.emptyMap(), entity, this.commonHeaders);
            response = elasticClientProxyService.performRequest(request);
        } else {
            response = elasticClientProxyService.performRequest(new Request(RequestMethod.POST.name(), api));
        }
        return response;
    }

    @Override
    public String executePutApi(String api, HttpEntity entity) {
        try {
            Response response = executePutApiReturnResponse(api, entity);
            String responseData = EntityUtils.toString(response.getEntity());
            return responseData;
        } catch (Exception e) {
            log.error("执行异常",  e );
            return e.getMessage();
        }
    }

    @Override
    public Response executePutApiReturnResponse(String api, HttpEntity entity) {
        try {
            CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
            ElasticClientProxyService elasticClientProxyService = getInstance(currentCluster);
            Response response;
            if (entity != null) {
                response = elasticClientProxyService.performRequest(buildRequest(RequestMethod.PUT.name(),
                        api, Collections.emptyMap(), entity, this.commonHeaders));
            } else {
                response = elasticClientProxyService.performRequest(new Request(RequestMethod.PUT.name(), api));
            }
            return response;
        } catch (Exception e) {
            log.error("执行异常,api:{}",api,  e );
            return null;
        }
    }

    @Override
    public boolean executeHeadApi(String api) {
        try {
            Request request = buildRequest("HEAD", api,
                    Collections.emptyMap(), null, new Header[0]);

            CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
            ElasticClientProxyService elasticClientProxyService = getInstance(currentCluster);

            Response response = elasticClientProxyService.performRequest(request);
            return (response.getStatusLine().getStatusCode() == 200);
        }catch (Exception e){
            log.error("执行异常",  e );
        }
        return false;
    }

    @Override
    public String executeDeleteApi(String apiUrl, HttpEntity entity) throws IOException {
        Response response = null;
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        ElasticClientProxyService elasticClientProxyService = getInstance(currentCluster);
        if (entity != null) {
            response = elasticClientProxyService.performRequest(buildRequest(RequestMethod.DELETE.name(), apiUrl, Collections.emptyMap(), entity, this.commonHeaders));
        } else {
            response = elasticClientProxyService.performRequest(new Request(RequestMethod.DELETE.name(), apiUrl));
        }
        String responseData = EntityUtils.toString(response.getEntity());
        return responseData;
    }


    public Request buildRequest(String method, String endPoint, Map<String, String> paramMap,
                                 HttpEntity entity, Header... headers) {
        Request request = new Request(method, endPoint);
        request.setEntity(entity);
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        if (paramMap != null && !paramMap.isEmpty()) {
            paramMap.forEach(builder::addParameter);
        }
        if (headers != null && headers.length > 0) {
            for (Header header : headers) {
                builder.addHeader(header.getName(), header.getValue());
            }
        }
        request.setOptions(builder);
        return request;
    }


}
