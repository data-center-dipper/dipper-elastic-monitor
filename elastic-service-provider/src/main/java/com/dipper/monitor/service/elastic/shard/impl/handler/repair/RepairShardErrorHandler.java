package com.dipper.monitor.service.elastic.shard.impl.handler.repair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.shard.impl.handler.AbstractShardErrorHandler;
import com.dipper.monitor.utils.ResultUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.nio.entity.NStringEntity;

import java.io.IOException;


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
                    RestoreIndexCreatedHandler restoreIndexCreatedHandler = new RestoreIndexCreatedHandler();
                    restoreIndexCreatedHandler.restoreIndexCreated(builder, index);
                    break;
                case "REPLICA_ADDED":
                    RestoreReolicaAddedHandler restoreReolicaAddedHandler = new RestoreReolicaAddedHandler();
                    restoreReolicaAddedHandler.restoreReolicaAdded(builder, index);
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




}
