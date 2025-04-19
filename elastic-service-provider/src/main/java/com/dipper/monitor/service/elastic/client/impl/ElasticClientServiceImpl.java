package com.dipper.monitor.service.elastic.client.impl;

import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.client.proxy.config.ElasticsearchBaseProxyConfig;
import com.dipper.client.proxy.config.PluginConfig;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.config.plugins.PluginsClientLoader;
import com.dipper.monitor.config.plugins.PluginsConfigUtils;
import com.dipper.monitor.constants.PluginConstants;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import com.dipper.monitor.utils.plugins.PluginConfigUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class ElasticClientServiceImpl implements ElasticClientService {

    private static final Cache<String, ElasticClientProxyService> clientCache = CacheBuilder.newBuilder()
            .maximumSize(50L)
            .expireAfterAccess(10L, TimeUnit.MINUTES)
            .concurrencyLevel(7)
            .build();


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

}
