package com.dipper.monitor.service.elastic.shard.impl.service.repair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.dipper.monitor.service.elastic.shard.impl.service.AbstractShardErrorHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class RestoreReolicaAddedHandler extends AbstractShardErrorHandler {

    public void restoreReolicaAdded(StringBuilder builder, String index) throws IOException {
        builder.append("增加分片副本不恰当\r\n");
        Integer nodeCount = Integer.valueOf(1);
        try {
            nodeCount = elasticRealNodeService.getClusterNodesCount();
        } catch (IOException e) {
            log.error("获取集群节点信息出错：{}", e.getMessage(), e);
            builder.append("获取集群节点信息出错：").append(e.getMessage()).append("\r\n");
        }

        String api = "/" + index + "/_settings";
        log.info("获取索引信息API:{}", api);
        builder.append("获取索引信息API:").append(api).append("\r\n");

        String setting = elasticClientService.executeGetApi(api);
        JSONObject obj = JSON.parseObject(setting);
        String numReolicas = (String) JSONPath.eval(obj, "$..settings.index.number_of_replicas[0]");

        Integer numReolicasInt = Integer.valueOf(Integer.parseInt(numReolicas));
        resetReplicas(builder, nodeCount, numReolicasInt, index);
    }
}
