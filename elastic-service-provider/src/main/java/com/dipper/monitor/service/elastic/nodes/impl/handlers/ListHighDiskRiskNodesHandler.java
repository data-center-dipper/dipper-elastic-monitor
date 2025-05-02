package com.dipper.monitor.service.elastic.nodes.impl.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * 获取Elastic节点磁盘使用率较高的前十条
 */
@Slf4j
public class ListHighDiskRiskNodesHandler {

    private final ElasticClientService elasticClientService;
    private static final int DISK_THRESHOLD = 80; // 默认阈值

    public ListHighDiskRiskNodesHandler(ElasticClientService elasticClientService) {
        this.elasticClientService = elasticClientService;
    }

    public List<ElasticNodeDisk> listHighDiskRiskNodes() throws IOException {
        long startTime = System.currentTimeMillis();
        String nodeInfoResult = elasticClientService.executeGetApi(ElasticRestApi.ES_NODES_STAT_MESSAGE.getApiPath());

        if (StringUtils.isBlank(nodeInfoResult) || nodeInfoResult.contains("master_not_discovered_exception")) {
            return Collections.emptyList();
        }
        log.info("Fetching nodes info from ES took: {} ms", System.currentTimeMillis() - startTime);

        JSONObject jsonObject = JSON.parseObject(nodeInfoResult);
        JSONObject nodesJson = jsonObject.getJSONObject("nodes");

        // 获取并解析磁盘使用信息
        Map<String, ElasticNodeDisk> nodeDiskMap = fetchAndParseDiskUsage();

        // 根据磁盘使用率筛选并排序节点
        List<ElasticNodeDisk> highDiskRiskNodes = filterAndSortNodes(nodesJson, nodeDiskMap);

        log.info("Total processing time: {} ms", System.currentTimeMillis() - startTime);
        return highDiskRiskNodes;
    }

    private Map<String, ElasticNodeDisk> fetchAndParseDiskUsage() throws IOException {
        String indexDiskResult = elasticClientService.executeGetApi(ElasticRestApi.INDEX_DISK_MESSAGE_JSON.getApiPath());
        JSONArray indexDiskJson = JSON.parseArray(indexDiskResult);
        Map<String, ElasticNodeDisk> nodeDiskMap = new HashMap<>();
        for (Object obj : indexDiskJson) {
            JSONObject jsonObj = (JSONObject) obj;
            ElasticNodeDisk esNodeDisk = parseDiskJson(jsonObj);
            nodeDiskMap.put(jsonObj.getString("node"), esNodeDisk);
        }
        return nodeDiskMap;
    }

    private ElasticNodeDisk parseDiskJson(JSONObject jsonObj) {
        Double diskPercent = jsonObj.getDouble("disk.percent");

        return new ElasticNodeDisk()
                .setShards(jsonObj.getInteger("shards"))
                .setDiskIndices(jsonObj.getString("disk.indices"))
                .setDiskUsed(jsonObj.getString("disk.used"))
                .setDiskAvail(jsonObj.getString("disk.avail"))
                .setDiskTotal(jsonObj.getString("disk.total"))
                .setDiskPercent(diskPercent)
                .setHost(jsonObj.getString("host"))
                .setIp(jsonObj.getString("ip"))
                .setNode(jsonObj.getString("node"))
                .setName(jsonObj.getString("node"));
    }

    private List<ElasticNodeDisk> filterAndSortNodes(JSONObject nodesJson, Map<String, ElasticNodeDisk> nodeDiskMap) {
        List<ElasticNodeDisk> nodeList = new ArrayList<>();
        nodesJson.forEach((id, nodeJson) -> {
            JSONObject nodeInfo = (JSONObject) nodeJson;
            String nodeName = nodeInfo.getString("name");
            if (nodeDiskMap.containsKey(nodeName)) {
                ElasticNodeDisk diskInfo = nodeDiskMap.get(nodeName);
//                if (diskInfo.getDiskPercent() >= DISK_THRESHOLD) {
                    nodeList.add(diskInfo);
//                }
            }
        });

        nodeList.sort((o1, o2) -> Double.compare(o2.getDiskPercent(), o1.getDiskPercent()));
        return nodeList;
    }
}