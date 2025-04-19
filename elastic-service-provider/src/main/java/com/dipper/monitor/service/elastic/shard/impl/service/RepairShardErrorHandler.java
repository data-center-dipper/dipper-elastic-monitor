package com.dipper.monitor.service.elastic.shard.impl.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.disk.DiskAllocationInfo;
import com.dipper.monitor.entity.elastic.disk.DiskWatermarkInfo;
import com.dipper.monitor.entity.elastic.nodes.service.EsNodeFailed;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.EsNodeInfo;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.alians.ElasticAliansService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.disk.ElasticDiskService;
import com.dipper.monitor.service.elastic.life.LifecyclePoliciesService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeService;
import com.dipper.monitor.service.elastic.shard.ShardService;
import com.dipper.monitor.utils.ResultUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.nio.entity.NStringEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RepairShardErrorHandler {

    private LifecyclePoliciesService lifecyclePoliciesService;
    private ShardService shardService;
    private ElasticClientService elasticClientService;
    private ElasticAliansService elasticAliansService;
    private ElasticNodeService elasticNodeService;
    private ElasticDiskService elasticDiskService;

    private StringBuilder builder = new StringBuilder(3000);

    public RepairShardErrorHandler(){
        lifecyclePoliciesService = SpringUtil.getBean(LifecyclePoliciesService.class);
        shardService = SpringUtil.getBean(ShardService.class);
        elasticClientService = SpringUtil.getBean(ElasticClientService.class);
        elasticAliansService = SpringUtil.getBean(ElasticAliansService.class);
        elasticNodeService = SpringUtil.getBean(ElasticNodeService.class);
        elasticDiskService = SpringUtil.getBean(ElasticDiskService.class);

        init();
    }

   
    private Cache<String, Object> cache;
    private static final String HENGXIAN = "\r\n---------------------------------------------------------\r\n";
    
    public void init() {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(7L)
                .expireAfterWrite(300L, TimeUnit.SECONDS)
                .expireAfterAccess(300L, TimeUnit.SECONDS)
                .concurrencyLevel(7)
                .build();
    }

    public String repairShardError() throws Exception {
        String result;
        try {
            result = elasticClientService.executeGetApi(ElasticRestApi.SHAED_UNASSIGNED_REASON.getApiPath());
        } catch (Exception e) {
            throw e;
        }
        if (StringUtils.isBlank(result)) {
            return "未发现未分配的";
        }

        String clusterHealth = elasticClientService.executeGetApi(ElasticRestApi.CLUSTER_HEALTH.getApiPath());
        JSONArray clusterHealthArray = JSON.parseArray(clusterHealth);
        JSONObject healthObj = (JSONObject)clusterHealthArray.get(0);
        String clusterStatus = healthObj.getString("status");
        if ("green".equalsIgnoreCase(clusterStatus)) {
            builder.append("集群信息为绿色，没有异常的分片\r\n");
        } else if ("yellow".equalsIgnoreCase(clusterStatus)) {
            builder.append("集群信息为黄色，主分片已经分配，副本分配未分配,该问题不影响使用，可修复，可不修复\r\n");
        } else {
            builder.append("集群信息为红色，主分片有部分未分配,该问题影响使用，必须修复\r\n");
        }
        builder.append("下面是未分配原因:\r\n")
                .append("\r\n---------------------------------------------------------\r\n").append("\r\n")
                .append(JSON.toJSONString(JSONObject.parseObject(result), new com.alibaba.fastjson.serializer.SerializerFeature[] { com.alibaba.fastjson.serializer.SerializerFeature.PrettyFormat })).append("\r\n")
                .append("\r\n---------------------------------------------------------\r\n");
        builder.append("下面对集群做一个相关的检测:\r\n");
        String index = (String) JSONPath.read(result, "$.index");
        String reason = (String)JSONPath.read(result, "$.unassigned_info.reason");
        if (StringUtils.isNotBlank(reason)) {
            reason = reason.toUpperCase();
        }
        checkDiskMessage();


        String indexSettings = elasticClientService.executeGetApi(index + "/_settings");
        JSONObject indexSettingsJson = JSON.parseObject(indexSettings);
        JSONArray numberOfreplicas = (JSONArray)JSONPath.eval(indexSettingsJson, "$..settings.index.number_of_replicas");
        JSONArray numberOfshards = (JSONArray)JSONPath.eval(indexSettingsJson, "$..settings.index.number_of_shards");
        String numberOfreplicasString = numberOfreplicas.get(0).toString();
        int numberOfreplicasInt = Integer.parseInt(numberOfreplicasString);

        EsNodeFailed esNodeFailed = elasticNodeService.getEsNodeFailed();
        Integer nodesTotal = esNodeFailed.getNodesTotal();
        Integer nodesSuccessful = esNodeFailed.getNodesSuccessful();
        Integer nodesFailed = esNodeFailed.getNodesFailed();

        builder.append("\r\n")
                .append("节点信息:").append("\r\n")
                .append("总节点:\t").append(nodesTotal).append("\t 在线节点:").append(nodesSuccessful).append("\t 离线节点:")
                .append(nodesFailed).append("\r\n")
                .append("\r\n")
                .append("分配信息:").append("\r\n")
                .append("shard数:\t").append(numberOfshards).append("\r\n")
                .append("副本数:\t").append(numberOfreplicas).append("\r\n")
                .append("\r\n");

        if (nodesSuccessful.intValue() < numberOfreplicasInt + 1) {
            builder.append("分片数目过多，而节点数不足:\r\n").append("建议的解决方案:").append("\r\n")
                    .append("PUT　/aaa-test/_settings\n{\n  \"number_of_replicas\":1\n}\n");
            builder.append("\r\n").append("这里的分片数，只能是小于").append(nodesSuccessful)
                    .append("个数，可根据实际情况进行修改，1-3个之间比较好");
        }

        processIndex(reason,indexSettingsJson,index,result);

        return builder.toString();
        }

    private void processIndex( String reason, JSONObject indexSettingsJson,String index,String result) {
        builder.append("\r\n").append("\r\n---------------------------------------------------------\r\n")
                .append("索引详情设置:").append("\r\n")
                .append(JSON.toJSONString(indexSettingsJson, new SerializerFeature[] { SerializerFeature.PrettyFormat }));
        builder.append("\r\n---------------------------------------------------------\r\n");
        builder.append("下面针对具体原因:").append(reason).append("的一些建议\r\n");
        switch (reason) {
            case "INDEX_CREATED":
                analyseIndexCreate(builder, index);
                break;
            case "CLUSTER_RECOVERED":
                builder.append("集群重启导致所有分片都被标记为未分配状态，等待一段时间再看看\r\n");
                break;
            case "INDEX_REOPENED":
                builder.append("open一个之前关闭的索引，repoen操作会将索引重新分配\r\n");
                break;
            case "DANGLING_INDEX_IMPORTED":
                builder.append("磁盘中存在，而集群状态中不存在的索引称为 dangling index\r\n");
                break;
            case "NEW_INDEX_RESTORED":
                builder.append("从快照恢复到一个新索引\r\n");
                break;
            case "EXISTING_INDEX_RESTORED":
                builder.append("从快照恢复到一个关闭状态的索引\r\n");
                break;
            case "REPLICA_ADDED":
                builder.append("增加分片副本不恰当，请修改副本\r\n");
                builder.append("PUT　/" + index + "/_settings\n{\n  \"number_of_replicas\":1\n}\n");
                break;
            case "ALLOCATION_FAILED":
                builder.append("由于分配失败导致，建议如下\r\n");
                if (StringUtils.isNotBlank(result) && result.contains("obtain in-memory shard local")) {
                    builder.append("问题原因: 原有分配未正常关闭和清理，所以当分片要重新分配回出问题节点的时候没办法获取分配锁,\n这不会造成分片数据丢失，只需要重新触发一下分配,\r\n");
                    builder.append("命令: POST /_cluster/reroute?retry_failed \r\n");
                    builder.append("其他命令如下:");
                }
                // 其他命令省略...
                break;
            case "NODE_LEFT":
                builder.append("节点离线,请查看节点为何离线，如果关闭了，请重启\r\n");
                return ;
            case "REROUTE_CANCELLED":
                builder.append("由于显式的cancel reroute命令\r\n");
                return ;
            case "REINITIALIZED":
                builder.append("由于分片从 started 状态转换到 initializing 状态\r\n");
                return ;
            case "REALLOCATED_REPLICA":
                builder.append("由于迁移分片副本\r\n");
                return ;
            case "PRIMARY_FAILED":
                builder.append("初始化副分片时，主分片失效\r\n");
                return ;
            case "FORCED_EMPTY_PRIMARY":
                builder.append("强制分配一个空的主分片\r\n");
                return ;
            case "MANUAL_ALLOCATION":
                builder.append("手工强制分配分片\r\n");
                return ;
        }
        builder.append("其他原因，暂未分析\r\n");
    }

    private void checkDiskMessage() throws IOException {
        DiskWatermarkInfo diskWatermark = elasticDiskService.getDiskWatermark();

        String enable = diskWatermark.getRoutingAllocationEnable();
        String diskThreshouldEnabled = diskWatermark.getDiskThreshouldEnabled();
        String updateInterval = diskWatermark.getUpdateInterval();

        builder.append("集群信息:").append("\r\n")
                .append("transient.cluster.routing.allocation.enable:").append(enable).append("\r\n")
                .append("transient.cluster.info.update.interval:").append(updateInterval).append("\r\n")
                .append("transient.cluster.routing.allocation.disk.threshould_enabled:").append(diskThreshouldEnabled).append("\r\n");
        if (StringUtils.isNotBlank(enable) && !"all".equalsIgnoreCase(enable)) {
            builder.append("建议执行命令:\r\n").append("PUT _cluster/settings\r\n{\r\n  \"transient\": {\r\n    \"cluster.routing.allocation.enable\":\"all\"\n  }\n}");
        }
        builder.append("\r\n---------------------------------------------------------\r\n");

        int highDiskNumber = diskWatermark.getHighDiskNumber();
        String lowDisk = diskWatermark.getLowDisk();

        builder.append("磁盘信息:").append("\r\n")
                .append("transient.cluster.routing.allocation.disk.watermark.high:").append(highDiskNumber).append("%")
                .append("\r\n").append("transient.cluster.routing.allocation.disk.watermark.low:")
                .append(lowDisk);

        List<DiskAllocationInfo> diskAllocations = elasticDiskService.getDiskAllocation();
        builder.append("\r\n").append("磁盘分配信息:\r\n");
        StringBuilder nodeDisOverflow = new StringBuilder();
        for (DiskAllocationInfo diskAllocationInfo : diskAllocations) {
            builder.append(diskAllocationInfo.toString()).append("\r\n");

            String diskPercent = diskAllocationInfo.getDiskPercent();
            if (diskPercent != null && StringUtils.isBlank(diskPercent)
                    && !diskPercent.equalsIgnoreCase("null")) {
                int currentDisk = (int)Math.round(Double.valueOf(diskPercent).doubleValue());
                if (highDiskNumber < currentDisk + 5) {
                    String node = diskAllocationInfo.getNode();
                    nodeDisOverflow.append("节点:").append(node)
                            .append("\t当前占用:").append(currentDisk).append("%")
                            .append("\t阈值：").append(highDiskNumber).append("%")
                            .append("\t该节点(即将)超过阈值").append("\r\n");
                }
            }
        }
        if (nodeDisOverflow.toString().length() < 2) {
            builder.append("\r\n").append("磁盘占用没有达到阈值，正常").append("\r\n");
        }
        builder.append("\r\n").append(nodeDisOverflow.toString()).append("\r\n");
        if (nodeDisOverflow.toString().length() > 2) {
            builder.append("建议的解决方案:").append("\r\n");
            builder.append("1. 磁盘不足，到索引管理中，删除部分不需要的索引，或者非常老的索引\r\n");
            builder.append("1. 磁盘不足，可以选择扩容ES集群\r\n");
            builder.append("2. 更改磁盘阈值设置,命令如下:\r\n").append("PUT　_cluster/settings\n{\n  \"transient\": {\n    \"cluster.routing.allocation.enable\":\"all\",\n    \"cluster.routing.allocation.disk.watermark.high\":\"0.9\",\n    \"cluster.routing.allocation.disk.watermark.low\":\"0.9\",\n    \"cluster.info.update.interval\":\"1m\",\n    \"cluster.routing.allocation.disk.threshold_enabled\":false\n  }\n}")
                    .append("\r\n").append("上面的命令根据自己的需要进行相关的修改或删除");
        }
        builder.append("\r\n---------------------------------------------------------\r\n");
    }


    public JSONObject setRoutingAllocation(String value) throws Exception {
        JSONObject response;
        String body = "{\n  \"transient\": {\n    \"cluster.routing.allocation.enable\":\"" + value + "\"\n  }\n}";

        NStringEntity nStringEntity = new NStringEntity(body);
        String result = elasticClientService.executePutApi(ElasticRestApi.CLUSTER_SETTING.getApiPath(),
                nStringEntity);
        JSONObject json1 = JSON.parseObject(result);

        if (json1.containsKey("acknowledged") && json1.getBooleanValue("acknowledged")) {
            log.info("操作成功");
            response = ResultUtils.onSuccessWithComment("操作成功");
        } else {
            log.info("操作成失败:{}", result);
            response = ResultUtils.onSuccessWithComment("操作成失败");
        }

        return response;
    }

// 方法`analyseIndexCreate`实现
        private void analyseIndexCreate(StringBuilder builder, String index) {
            builder.append("出现这个原因多半是因为索引创建的有问题，比如副本数过多，适当的降低副本数,修改副本数的命令如下:\r\n");
            builder.append("PUT　/" + index + "/_settings\n{\n  \"number_of_replicas\":1\n}\n");
        }

        public String shardRestore() throws IOException {
            StringBuilder builder = new StringBuilder();
            builder.append("开始进行常用的分片异常恢复功能...\r\n");
            String result = null;
            try {
                result = elasticClientService.executeGetApi(ElasticRestApi.SHAED_UNASSIGNED_REASON.getApiPath());
            } catch (Exception e) {
                log.error("分片分配信息获取异常", e);
            }
            if (StringUtils.isBlank(result)) {
                builder.append("未发现未分配的\r\n");
            }
            String index = (String)JSONPath.read(result, "$.index");
            String reason = (String)JSONPath.read(result, "$.unassigned_info.reason");
            if (StringUtils.isNotBlank(reason)) {
                reason = reason.toUpperCase();
            }
            log.info("未分配的原因：{}", reason);
            String clusterSetting = elasticClientService.executeGetApi(ElasticRestApi.CLUSTER_SETTING.getApiPath());
            JSONObject clusteringSettingJson = JSON.parseObject(clusterSetting);
            String enable = (String)JSONPath.eval(clusteringSettingJson, "$.transient.cluster.routing.allocation.enable");
            if (StringUtils.isNotBlank(enable) && !"all".equalsIgnoreCase(enable)) {
                builder.append("检测到集群分片自动分配功能被关闭....\r\n");
                builder.append("检测到集群分片自动分配功能被关闭....开启中\r\n");
                builder.append("执行命令:\r\n").append("PUT _cluster/settings\r\n{\r\n  \"transient\": {\r\n    \"cluster.routing.allocation.enable\":\"all\"\n  }\n}");
                try {
                    JSONObject result1 = setRoutingAllocation("all");
                    builder.append(result1).append("\r\n");
                } catch (Exception e) {
                    log.info("开启集群分片自动分配功能失败:{}", e.getMessage(), e);
                    builder.append("执行命令异常：").append(e.getMessage()).append("\r\n");
                }
            }
            switch (reason) {
                case "INDEX_CREATED":
                    restoreIndexCreated(builder, index);
                    break;
                case "REPLICA_ADDED":
                    restoreReolicaAdded(builder, index);
                    break;
                case "ALLOCATION_FAILED":
                    builder.append("由于分配失败导致，建议如下\r\n");
                    if (StringUtils.isNotBlank(result) && result.contains("obtain in-memory shard local")) {
                        builder.append("问题原因: 原有分配未正常关闭和清理，所以当分片要重新分配回出问题节点的时候没办法获取分配锁,\n这不会造成分片数据丢失，只需要重新触发一下分配,\r\n");
                        builder.append("命令: POST /_cluster/reroute?retry_failed \r\n");
                        String str = elasticClientService.executePostApi("/_cluster/reroute?retry_failed", null);
                        builder.append(str).append("\r\n");
                    }
                    break;
            }
            builder.append("执行").append(ElasticRestApi.SHAED_FORCE_DISTRIBUTION.getApiPath())
                    .append(ElasticRestApi.SHAED_FORCE_DISTRIBUTION.getDescription()).append("\r\n");
            String retryFailed = elasticClientService.executePostApi(ElasticRestApi.SHAED_FORCE_DISTRIBUTION.getApiPath(), null);
            builder.append(retryFailed).append("\r\n");
            builder.append("执行完毕");

            return builder.toString();
        }

    private void restoreIndexCreated(StringBuilder builder, String index) throws IOException {
        String api = "/" + index + "/_settings";
        log.info("获取索引信息API:{}", api);
        builder.append("获取索引信息API:").append(api).append("\r\n");

        String setting = elasticClientService.executeGetApi(api);
        JSONObject obj = JSON.parseObject(setting);

        String numReolicas = (String)JSONPath.eval(obj, "$..settings.index.number_of_replicas[0]");
        Integer numReolicasInt = Integer.valueOf(Integer.parseInt(numReolicas));

        Integer nodeCount = Integer.valueOf(1);
        try {
            nodeCount = elasticNodeService.getClusterNodesCount();
        } catch (IOException e) {
            log.error("获取集群节点信息出错：{}", e.getMessage(), e);
            builder.append("获取集群节点信息出错：").append(e.getMessage()).append("\r\n");
        }

        if (nodeCount.intValue() < numReolicasInt.intValue() + 1) {
            resetReplicas(builder, nodeCount, numReolicasInt, index);
        }
    }

    private void restoreReolicaAdded(StringBuilder builder, String index) throws IOException {
        builder.append("增加分片副本不恰当\r\n");
        Integer nodeCount = Integer.valueOf(1);
        try {
            nodeCount = elasticNodeService.getClusterNodesCount();
        } catch (IOException e) {
            log.error("获取集群节点信息出错：{}", e.getMessage(), e);
            builder.append("获取集群节点信息出错：").append(e.getMessage()).append("\r\n");
        }

        String api = "/" + index + "/_settings";
        log.info("获取索引信息API:{}", api);
        builder.append("获取索引信息API:").append(api).append("\r\n");

        String setting = elasticClientService.executeGetApi(api);
        JSONObject obj = JSON.parseObject(setting);
        String numReolicas = (String)JSONPath.eval(obj, "$..settings.index.number_of_replicas[0]");

        Integer numReolicasInt = Integer.valueOf(Integer.parseInt(numReolicas));
        resetReplicas(builder, nodeCount, numReolicasInt, index);
    }

    private void resetReplicas(StringBuilder builder, Integer nodeCount, Integer numReolicasInt, String index) throws UnsupportedEncodingException, UnsupportedEncodingException {
        String api = "/" + index + "/_settings";
        if (nodeCount.intValue() < numReolicasInt.intValue()) {
            numReolicasInt = Integer.valueOf(nodeCount.intValue() / 2);
            builder.append("本次将副本设置为节点数的一半:").append(nodeCount).append("\r\n");
        } else {
            numReolicasInt = Integer.valueOf(numReolicasInt.intValue() / 2);
            builder.append("本次将副本设置为当前的一半:").append(numReolicasInt).append("\r\n");
        }
        log.info("计算最终副本数大小：{}", numReolicasInt);
        builder.append("计算最终副本数大小:").append(numReolicasInt).append("\r\n");

        String body = "{\n  \"number_of_replicas\":" + numReolicasInt + "\n}\n";

        builder.append("PUT ").append(api).append("\r\n").append(body);
        NStringEntity nStringEntity = new NStringEntity(body);
        String result2 = elasticClientService.executePutApi(api, (HttpEntity)nStringEntity);
        builder.append("设置新的分片：").append("\r\n")
                .append(result2).append("\r\n");
        builder.append("修改完毕，请刷新，如果还是这个问题，请再次点击修复").append("\r\n");
    }
}
