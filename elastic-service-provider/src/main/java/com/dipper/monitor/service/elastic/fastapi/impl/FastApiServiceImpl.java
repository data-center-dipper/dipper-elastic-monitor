package com.dipper.monitor.service.elastic.fastapi.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.fastapi.FastApiDefView;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.fastapi.FastApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FastApiServiceImpl implements FastApiService {

    @Autowired
    private ElasticClientService elasticClientService;

    private List<FastApiDefView> apiList = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadApiDefinitions();
    }

    /**
     * 从YAML文件加载API定义
     */
    private void loadApiDefinitions() {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("fastApiDefList.yaml");
            if (inputStream == null) {
                log.error("无法找到fastApiDefList.yaml文件");
                // 创建一些默认API
                createDefaultApis();
                return;
            }
            
            Map<String, List<Map<String, Object>>> data = yaml.load(inputStream);
            List<Map<String, Object>> apis = data.get("apis");
            
            if (apis != null) {
                for (Map<String, Object> api : apis) {
                    FastApiDefView apiDef = new FastApiDefView();
                    apiDef.setId(UUID.randomUUID().toString());
                    apiDef.setApiName((String) api.get("name"));
                    apiDef.setApiDesc((String) api.get("description"));
                    apiDef.setMethod((String) api.get("method"));
                    apiDef.setApiPath((String) api.get("path"));
                    apiDef.setBody((String) api.get("body"));
                    apiDef.setHeaders((String) api.get("headers"));
                    apiList.add(apiDef);
                }
            } else {
                log.warn("YAML文件中没有找到API定义，创建默认API");
                createDefaultApis();
            }
        } catch (Exception e) {
            log.error("加载API定义失败", e);
            createDefaultApis();
        }
    }

    /**
     * 创建默认API
     */
    private void createDefaultApis() {
        // 添加一些默认API
        FastApiDefView api1 = new FastApiDefView();
        api1.setId(UUID.randomUUID().toString());
        api1.setApiName("查看节点信息");
        api1.setApiDesc("列出集群中的所有节点信息");
        api1.setMethod("GET");
        api1.setApiPath("/_cat/nodes?v");
        apiList.add(api1);

        FastApiDefView api2 = new FastApiDefView();
        api2.setId(UUID.randomUUID().toString());
        api2.setApiName("集群健康状态");
        api2.setApiDesc("获取集群健康状态信息");
        api2.setMethod("GET");
        api2.setApiPath("/_cluster/health?pretty");
        apiList.add(api2);

        FastApiDefView api3 = new FastApiDefView();
        api3.setId(UUID.randomUUID().toString());
        api3.setApiName("索引搜索");
        api3.setApiDesc("对指定索引进行简单搜索");
        api3.setMethod("GET");
        api3.setApiPath("/logs-*/_search");
        api3.setBody("{\n  \"query\": {\n    \"match_all\": {}\n  }\n}");
        apiList.add(api3);
    }

    @Override
    public List<FastApiDefView> fastApiList(String nameLike) {
        if (nameLike == null || nameLike.isEmpty()) {
            return apiList;
        }
        
        return apiList.stream()
                .filter(api -> api.getApiName().contains(nameLike) || 
                               api.getApiDesc().contains(nameLike) ||
                               api.getApiPath().contains(nameLike))
                .collect(Collectors.toList());
    }

    @Override
    public String transToCurl(FastApiDefView fastApiDefView) {
        StringBuilder curl = new StringBuilder();
        curl.append("curl -X ").append(fastApiDefView.getMethod()).append(" ");
        
        // 添加请求头
        if (fastApiDefView.getHeaders() != null && !fastApiDefView.getHeaders().isEmpty()) {
            String[] headers = fastApiDefView.getHeaders().split("\n");
            for (String header : headers) {
                if (!header.trim().isEmpty()) {
                    curl.append("-H \"").append(header.trim()).append("\" ");
                }
            }
        } else {
            curl.append("-H \"Content-Type: application/json\" ");
        }
        
        // 添加URL
        curl.append("'http://localhost:9200").append(fastApiDefView.getApiPath()).append("' ");
        
        // 添加请求体
        if (fastApiDefView.getBody() != null && !fastApiDefView.getBody().isEmpty()) {
            curl.append("-d '").append(fastApiDefView.getBody().replace("'", "\\'")).append("'");
        }
        
        return curl.toString();
    }

    @Override
    public String executeFastApi(FastApiDefView fastApiDefView) {
        try {
            String method = fastApiDefView.getMethod();
            String path = fastApiDefView.getApiPath();
            String body = fastApiDefView.getBody();
            
            String response;
            switch (method.toUpperCase()) {
                case "GET":
                    response = elasticClientService.executeGetApi(path);
                    break;
                case "POST":
                    StringEntity entity = new StringEntity(body != null ? body : "", ContentType.APPLICATION_JSON);
                    response = elasticClientService.executePostApi(path, entity);
                    break;
                case "PUT":
                    StringEntity putEntity = new StringEntity(body != null ? body : "", ContentType.APPLICATION_JSON);
                    response = elasticClientService.executePutApi(path, putEntity);
                    break;
                case "DELETE":
                    StringEntity deleteEntity = body != null ? new StringEntity(body, ContentType.APPLICATION_JSON) : null;
                    response = elasticClientService.executeDeleteApi(path, deleteEntity);
                    break;
                case "HEAD":
                    boolean exists = elasticClientService.executeHeadApi(path);
                    response = JSONObject.toJSONString(Map.of("exists", exists));
                    break;
                default:
                    throw new IllegalArgumentException("不支持的HTTP方法: " + method);
            }
            
            return response;
        } catch (IOException e) {
            log.error("执行API失败", e);
            return JSON.toJSONString(Map.of("error", e.getMessage()));
        }
    }
}
