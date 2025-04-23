package com.dipper.monitor.service.elastic.nodes.impl.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDetail;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 获取节点 内存压力比较大的前十条 按照内存压力大小排序
 */
@Slf4j
public class ListHighMemoryRiskNodesHandler {

    private final ElasticClientService elasticClientService;

    public ListHighMemoryRiskNodesHandler(ElasticClientService elasticClientService) {
        this.elasticClientService = elasticClientService;
    }

    public List<ElasticNodeDetail> listHighRiskNodes() throws IOException {
        long startTime = System.currentTimeMillis();
        String nodeInfoResult = elasticClientService.executeGetApi(ElasticRestApi.ES_NODES_MESSAGE.getApiPath());
        if (StringUtils.isBlank(nodeInfoResult) || nodeInfoResult.contains("master_not_discovered_exception")) {
            return Collections.emptyList();
        }
        log.info("Time to fetch nodes info from ES: {} ms", System.currentTimeMillis() - startTime);

        JSONObject jsonObject = JSON.parseObject(nodeInfoResult);
        JSONObject nodes = jsonObject.getJSONObject("nodes");

        // 提取节点列表并排序
        List<ElasticNodeDetail> nodeList = extractAndProcessNodes(nodes);
        nodeList.sort(this::compareScores);

        log.info("Total time taken: {} ms", System.currentTimeMillis() - startTime);
        return nodeList.stream().limit(10).collect(Collectors.toList()); // 只返回前10个内存压力最大的节点
    }

    private List<ElasticNodeDetail> extractAndProcessNodes(JSONObject nodes) {
        List<ElasticNodeDetail> nodeList = new ArrayList<>();
        nodes.forEach((key, value) -> {
            JSONObject nodeJson = (JSONObject) value;
            ElasticNodeDetail nodeDetail = buildElasticNodeDetail(nodeJson);
            nodeList.add(nodeDetail);
        });
        return nodeList;
    }

    private ElasticNodeDetail buildElasticNodeDetail(JSONObject nodeJson) {
        return new ElasticNodeDetail()
                .setName(getSafeString(nodeJson, "name"))
                .setTransportAddress(getSafeString(nodeJson, "transport_address"))
                .setRoles(getSafeArrayAsString(nodeJson, "roles"))
                .setCpuPercent(getSafeIntegerFromNestedJson(nodeJson, "os.cpu.percent"))
                .setOsMemTotal(getSizeInGB(getSafeLongFromNestedJson(nodeJson, "os.mem.total_in_bytes")))
                .setOsMemFree(getSizeInGB(getSafeLongFromNestedJson(nodeJson, "os.mem.free_in_bytes")))
                .setOsMemUsed(getSizeInGB(getSafeLongFromNestedJson(nodeJson, "os.mem.used_in_bytes")))
                .setOsMemusedPercent(getSafeIntegerFromNestedJson(nodeJson, "os.mem.used_percent"))
                .setOsMemFreePercent(getSafeIntegerFromNestedJson(nodeJson, "os.mem.free_percent"))
                .setJvmMemHeapused(getSizeInGB(getSafeLongFromNestedJson(nodeJson, "jvm.mem.heap_used_in_bytes")))
                .setJvmMemHeapusedPrecent(getSafeIntegerFromNestedJson(nodeJson, "jvm.mem.heap_used_percent"))
                .setJvmMemHeapMax(getSizeInGB(getSafeLongFromNestedJson(nodeJson, "jvm.mem.heap_max_in_bytes")))
                .setOpenFileDescriptors(getSafeIntegerFromNestedJson(nodeJson, "process.open_file_descriptors"))
                .setMaxFileDescriptors(getSafeIntegerFromNestedJson(nodeJson, "process.max_file_descriptors"))
                .setThreadsCount(getSafeInteger(nodeJson, "threads"))
                .setHostIp(getSafeString(nodeJson, "host"))
                .setEsJvmThreshouldInt(90)
                .setEsDiskThreshouldInt(80)
                .setScore(getSafeIntegerFromNestedJson(nodeJson, "jvm.mem.heap_used_percent"));
    }

    // 辅助方法用于安全获取JSON中的值
    private String getSafeString(JSONObject json, String key) {
        return Optional.ofNullable(json.getString(key)).orElse("");
    }

    private Integer getSafeInteger(JSONObject json, String key) {
        return Optional.ofNullable(json.getInteger(key)).orElse(null);
    }

    private Long getSafeLong(JSONObject json, String key) {
        return Optional.ofNullable(json.getLong(key)).orElse(null);
    }

    private String getSafeArrayAsString(JSONObject json, String key) {
        JSONArray array = json.getJSONArray(key);
        return array != null ? array.toJavaList(String.class).toString() : "";
    }

    private Integer getSafeIntegerFromNestedJson(JSONObject json, String nestedKey) {
        String[] keys = nestedKey.split("\\.");
        Object value = json;
        for (String k : keys) {
            if (!(value instanceof JSONObject)) break;
            value = ((JSONObject) value).get(k);
        }
        return value instanceof Integer ? (Integer) value : null;
    }

    private Long getSafeLongFromNestedJson(JSONObject json, String nestedKey) {
        String[] keys = nestedKey.split("\\.");
        Object value = json;
        for (String k : keys) {
            if (!(value instanceof JSONObject)) break;
            value = ((JSONObject) value).get(k);
        }
        return value instanceof Long ? (Long) value : null;
    }

    private Double getSizeInGB(Long bytes) {
        return bytes != null ? bytes / 1073741824.0 : null;
    }

    private int compareScores(ElasticNodeDetail o1, ElasticNodeDetail o2) {
        return Comparator.comparing(ElasticNodeDetail::getScore).reversed().compare(o1, o2);
    }
}