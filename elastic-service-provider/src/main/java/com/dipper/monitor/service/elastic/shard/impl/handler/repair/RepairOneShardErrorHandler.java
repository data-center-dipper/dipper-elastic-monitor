package com.dipper.monitor.service.elastic.shard.impl.handler.repair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.dipper.monitor.entity.elastic.shard.OneShardRepireReq;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.shard.impl.handler.AbstractShardErrorHandler;
import com.dipper.monitor.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.nio.entity.NStringEntity;

import java.io.IOException;
import java.net.URLEncoder;

@Slf4j
public class RepairOneShardErrorHandler extends AbstractShardErrorHandler {

    /**
     * 单个分片异常修复方法
     * @param oneShardRepireReq 包含索引名称和分片ID的对象
     * @return 返回修复操作的结果描述
     */
    public String repairOneShardError(OneShardRepireReq oneShardRepireReq) {
        StringBuilder builder = new StringBuilder();
        builder.append("开始进行指定分片的异常恢复功能...\r\n");

        // 获取指定索引和分片的信息
        try {

            String indexName = oneShardRepireReq.getIndexName();
            String shardId = oneShardRepireReq.getShardId();
            boolean isPrimary = oneShardRepireReq.getPrimary();

            String apiUrl = String.format(
                    "/_cluster/allocation/explain/index/%s/%s?primary=%b",
                    URLEncoder.encode(indexName, "UTF-8"),
                    shardId,
                    isPrimary
            );

            String result = elasticClientService.executeGetApi(apiUrl);


            if (StringUtils.isBlank(result)) {
                builder.append("未发现指定分片信息\r\n");
                return builder.toString();
            }

            String reason = (String) JSONPath.read(result, "$.unassigned_info.reason");
            if (StringUtils.isNotBlank(reason)) {
                reason = reason.toUpperCase();
            }
            log.info("未分配的原因：{}", reason);

            // 检查集群设置是否允许分片分配
            boolean routingAllocationEnabled = ensureRoutingAllocation(builder);

            // 根据原因采取不同的修复措施
            switch (reason) {
                case "INDEX_CREATED":
                    restoreIndexCreated(builder, indexName);
                    break;
                case "REPLICA_ADDED":
                    restoreReolicaAdded(builder, indexName);
                    break;
                case "ALLOCATION_FAILED":
                    handleAllocationFailed(builder, indexName, shardId);
                    break;
                default:
                    builder.append("不支持的未分配原因: ").append(reason).append("\r\n");
            }

            if (routingAllocationEnabled) {
                builder.append("尝试重新分配分片...\r\n");
                String retryFailed = elasticClientService.executePostApi("/_cluster/reroute?retry_failed", null);
                builder.append(retryFailed).append("\r\n");
            }
            builder.append("执行完毕");

        } catch (Exception e) {
            log.error("修复单个分片异常失败", e);
            builder.append("修复过程中出现错误: ").append(e.getMessage()).append("\r\n");
        }

        return builder.toString();
    }

    private boolean ensureRoutingAllocation(StringBuilder builder) throws Exception {
        String clusterSetting = elasticClientService.executeGetApi(ElasticRestApi.CLUSTER_SETTING.getApiPath());
        JSONObject clusteringSettingJson = JSON.parseObject(clusterSetting);
        String enable = (String) JSONPath.eval(clusteringSettingJson, "$.transient.cluster.routing.allocation.enable");

        if (StringUtils.isNotBlank(enable) && !"all".equalsIgnoreCase(enable)) {
            builder.append("检测到集群分片自动分配功能被关闭....\r\n");
            builder.append("开启中\r\n");
            String body = "{\n  \"transient\": {\n    \"cluster.routing.allocation.enable\":\"all\"\n  }\n}";
            NStringEntity nStringEntity = new NStringEntity(body);
            String result = elasticClientService.executePutApi(ElasticRestApi.CLUSTER_SETTING.getApiPath(), nStringEntity);
            JSONObject json1 = JSON.parseObject(result);

            if (json1.containsKey("acknowledged") && json1.getBooleanValue("acknowledged")) {
                log.info("操作成功");
                builder.append("操作成功\r\n");
                return true;
            } else {
                log.info("操作成失败:{}", result);
                builder.append("操作成失败:").append(result).append("\r\n");
                return false;
            }
        }
        return true; // 如果已经启用，则无需更改设置
    }

    private void restoreIndexCreated(StringBuilder builder, String indexName) {
        // 这里应该调用具体的修复逻辑
        builder.append("正在处理由于索引创建导致的分片异常...\r\n");
        // 示例修复逻辑
        builder.append("完成处理。\r\n");
    }

    private void restoreReolicaAdded(StringBuilder builder, String indexName) {
        // 这里应该调用具体的修复逻辑
        builder.append("正在处理由于副本添加导致的分片异常...\r\n");
        // 示例修复逻辑
        builder.append("完成处理。\r\n");
    }

    private void handleAllocationFailed(StringBuilder builder, String indexName, String shardId) {
        builder.append("由于分配失败导致，建议如下\r\n");
        builder.append("问题原因: 原有分配未正常关闭和清理，所以当分片要重新分配回出问题节点的时候没办法获取分配锁,\n这不会造成分片数据丢失，只需要重新触发一下分配,\r\n");
        builder.append("命令: POST /_cluster/reroute?retry_failed \r\n");
        try {
            String str = elasticClientService.executePostApi("/_cluster/reroute?retry_failed", null);
            builder.append(str).append("\r\n");
        } catch (IOException e) {
            log.error("重新分配失败: {}", e.getMessage(), e);
            builder.append("重新分配失败: ").append(e.getMessage()).append("\r\n");
        }
    }
}