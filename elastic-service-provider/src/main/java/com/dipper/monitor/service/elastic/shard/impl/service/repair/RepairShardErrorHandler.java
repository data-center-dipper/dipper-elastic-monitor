package com.dipper.monitor.service.elastic.shard.impl.service.repair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.disk.DiskAllocationInfo;
import com.dipper.monitor.entity.elastic.disk.DiskWatermarkInfo;
import com.dipper.monitor.entity.elastic.nodes.service.EsNodeFailed;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.nio.entity.NStringEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RepairShardErrorHandler  extends AbstractShardErrorHandler {

   




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


        public String repairShardError() throws IOException {
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
