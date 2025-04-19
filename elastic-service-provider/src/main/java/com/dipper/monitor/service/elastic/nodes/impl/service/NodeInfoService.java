package com.dipper.monitor.service.elastic.nodes.impl.service;

import com.dipper.client.proxy.api.elasticsearch.ElasticClientProxyService;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.EsNodeInfo;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NodeInfoService {

    private static final Logger logger = LoggerFactory.getLogger(NodeInfoService.class);

    private ElasticClientProxyService clientProxyService;
    
    public NodeInfoService(ElasticClientProxyService clientProxyService) {
        this.clientProxyService = clientProxyService;
    }

    public List<EsNodeInfo> getEsNodes() throws IOException {
        Request request = new Request("GET", "/_nodes");
        Response response = clientProxyService.performRequest(request);
        String httpResult = EntityUtils.toString(response.getEntity(), "UTF-8");
        logger.debug("获取返回值信息：{}", httpResult);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(httpResult);

        List<EsNodeInfo> esNodeInfos = new ArrayList<>();

        if (rootNode.hasNonNull("nodes")) {
            Iterator<String> nodeIds = rootNode.path("nodes").fieldNames();
            while (nodeIds.hasNext()) {
                String nodeId = nodeIds.next();
                JsonNode node = rootNode.path("nodes").get(nodeId);

                EsNodeInfo esNodeInfo = new EsNodeInfo();

                esNodeInfo.setName(getSafeTextValue(node, "name"));
                esNodeInfo.setIp(getSafeTextValue(node, "ip"));
                esNodeInfo.setHost(getSafeTextValue(node, "host"));
                esNodeInfo.setVersion(getSafeTextValue(node, "version"));
                esNodeInfo.setRoles(parseStringList(node, "roles"));

                // Settings
                Settings settings = new Settings();
                settings.setClusterName(getSafeTextValue(rootNode.path("cluster_name"), ""));
                settings.setInitialMasterNodes(parseStringList(rootNode.path("nodes").get(nodeId).path("settings").path("cluster").path("initial_master_nodes"), ""));

                // JVM Info
                JvmInfo jvmInfo = parseJvmInfo(node.path("jvm"));

                // OS Info
                OsInfo osInfo = parseOsInfo(node.path("os"));

                // Path Info
                PathInfo pathInfo = parsePathInfo(rootNode.path("nodes").get(nodeId).path("settings").path("path"));

                // Thread Pool
                ThreadPool threadPool = parseThreadPool(node.path("thread_pool"));

                settings.setThreadPool(threadPool);

                esNodeInfo.setSettings(settings);
                esNodeInfo.setTotalIndexingBuffer(0L); // 假设这个值需要从其他地方获取或计算
                esNodeInfo.setMaster(false); // 假设这个值需要从其他地方获取或计算
                esNodeInfo.setJvmInfo(jvmInfo);
                esNodeInfo.setOsInfo(osInfo);
                esNodeInfo.setPathInfo(pathInfo);

                esNodeInfos.add(esNodeInfo);
            }
        }

        return esNodeInfos;
    }

    private String getSafeTextValue(JsonNode node, String fieldName) {
        return node.hasNonNull(fieldName) ? node.get(fieldName).asText() : "";
    }

    private List<String> parseStringList(JsonNode node, String fieldName) {
        List<String> list = new ArrayList<>();
        if (node.hasNonNull(fieldName)) {
            for (JsonNode item : node.get(fieldName)) {
                list.add(item.asText());
            }
        }
        return list;
    }

    private JvmInfo parseJvmInfo(JsonNode jvmNode) {
        JvmInfo jvmInfo = new JvmInfo();
        jvmInfo.setPid(getSafeTextValue(jvmNode.path("pid"), ""));
        jvmInfo.setStartTimeInMillis(jvmNode.path("start_time_in_millis").asLong(0));
        jvmInfo.setHeapInitInBytes(jvmNode.path("mem").path("heap_init_in_bytes").asLong(0));
        jvmInfo.setHeapMaxInBytes(jvmNode.path("mem").path("heap_max_in_bytes").asLong(0));
        jvmInfo.setNonHeapMaxInBytes(jvmNode.path("mem").path("non_heap_max_in_bytes").asLong(0));
        jvmInfo.setDirectMaxInBytes(jvmNode.path("mem").path("direct_max_in_bytes").asLong(0));
        return jvmInfo;
    }

    private OsInfo parseOsInfo(JsonNode osNode) {
        OsInfo osInfo = new OsInfo();
        osInfo.setPrettyName(getSafeTextValue(osNode.path("pretty_name"), ""));
        osInfo.setArch(getSafeTextValue(osNode.path("arch"), ""));
        return osInfo;
    }

    private PathInfo parsePathInfo(JsonNode pathNode) {
        PathInfo pathInfo = new PathInfo();
        pathInfo.setData(getSafeTextValue(pathNode.path("data"), ""));
        pathInfo.setLogs(getSafeTextValue(pathNode.path("logs"), ""));
        pathInfo.setHome(getSafeTextValue(pathNode.path("home"), ""));
        return pathInfo;
    }

    private ThreadPool parseThreadPool(JsonNode threadPoolNode) {
        ThreadPool threadPool = new ThreadPool();
        threadPool.setWriteQueueSize(threadPoolNode.path("write").path("queue_size").asLong(-1));
        threadPool.setWriteSize(threadPoolNode.path("write").path("size").asLong(0));
        threadPool.setForceMerge(threadPoolNode.path("force_merge").path("size").asLong(0));
        threadPool.setSearchCoordination(threadPoolNode.path("search_coordination").path("size").asLong(0));
        threadPool.setSearch(threadPoolNode.path("search").path("size").asLong(0));
        return threadPool;
    }

}
