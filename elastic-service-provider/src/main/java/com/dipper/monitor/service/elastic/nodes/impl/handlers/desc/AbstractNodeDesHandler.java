package com.dipper.monitor.service.elastic.nodes.impl.handlers.desc;

import com.dipper.monitor.entity.elastic.original.nodes.info.EsNodeInfo;
import com.dipper.monitor.entity.elastic.original.nodes.info.nodes.*;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public abstract class AbstractNodeDesHandler {

    protected ElasticClientService elasticClientService;

    public AbstractNodeDesHandler(ElasticClientService elasticClientService) {
        this.elasticClientService = elasticClientService;
    }



    public List<EsNodeInfo> getEsNodes() throws IOException {
        String httpResult = elasticClientService.executeGetApi("/_nodes");
        log.debug("获取返回值信息：{}", httpResult);

        List<EsNodeInfo> esNodeInfos = parseReponse(httpResult);
        return esNodeInfos;
    }

    protected List<EsNodeInfo> parseReponse(String httpResult) throws JsonProcessingException {

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
        osInfo.setPrettyName(getSafeTextValue(osNode, "pretty_name"));
        osInfo.setArch(getSafeTextValue(osNode, "arch"));
        osInfo.setAvailableProcessors(getSafeTextValue(osNode, "available_processors"));
        osInfo.setAllocatedProcessors(getSafeTextValue(osNode, "allocated_processors"));
        return osInfo;
    }

    private PathInfo parsePathInfo(JsonNode pathNode) {
        PathInfo pathInfo = new PathInfo();
        pathInfo.setData(getSafeTextValue(pathNode, "data"));
        pathInfo.setLogs(getSafeTextValue(pathNode, "logs"));
        pathInfo.setHome(getSafeTextValue(pathNode, "home"));
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
