package com.dipper.monitor.service.elastic.setting.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.shard.recovery.AllocationEnableReq;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.setting.ClusterSettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClusterSettingServiceImpl implements ClusterSettingService {


    @Autowired
    private ElasticClientService elasticClientService;

    /**
     * PUT /_cluster/settings
     * {
     *   "persistent": {
     *     "cluster.routing.allocation.enable": "none"
     *   }
     * }
     * PUT /_cluster/settings
     * {
     *   "persistent": {
     *     "cluster.routing.allocation.enable": "all"
     *   }
     * }
     * @param allocationEnableReq
     */
    @Override
    public void enableOrCloseShardAllocation(AllocationEnableReq allocationEnableReq) {
        String enable = allocationEnableReq.getEnable();

        if (!"all".equals(enable) && !"none".equals(enable)) {
            throw new IllegalArgumentException("参数 enable 必须为 'all' 或 'none'");
        }

        try {
            // ✅ 正确的格式：只包含必要的层级
            JSONObject requestBody = new JSONObject();
            JSONObject persistent = new JSONObject();
            persistent.put("cluster.routing.allocation.enable", enable);
            requestBody.put("persistent", persistent);

            log.info("执行参数:{}", requestBody.toJSONString());

            StringEntity entity = new StringEntity(
                    requestBody.toJSONString(),
                    ContentType.APPLICATION_JSON
            );

            String response = elasticClientService.executePutApi("/_cluster/settings", entity);
            log.info("操作成功响应: {}", response);

        } catch (Exception e) {
            log.error("更新集群设置失败", e);
            throw new RuntimeException("更新集群设置失败: " + e.getMessage(), e);
        }
    }

        @Override
        public String getShardAllocation() {
            try {
                // 调用封装好的 GET 接口
                String response = elasticClientService.executeGetApi("/_cluster/settings");

                // 解析 JSON 响应
                JSONObject settings = JSONObject.parseObject(response);
                JSONObject persistent = settings.getJSONObject("persistent");

                if (persistent != null) {
                    String enableValue = persistent.getString("cluster.routing.allocation.enable");
                    log.info("获取集群设置成功: {}", enableValue);
                    return enableValue != null ? enableValue : "all";
                } else {
                    log.warn("获取集群设置成功: all");
                    return "all";
                }

            } catch (Exception e) {
                log.error("获取集群设置失败", e);
                throw new RuntimeException("获取集群设置失败: " + e.getMessage(), e);
            }
        }


    /**
     * PUT _cluster/settings
     * {
     *   "transient": {
     *     "cluster.routing.allocation.exclude._ip": "10.0.0.11"
     *   }
     * }
     * @param nodeName 要下线的节点名称或IP
     */
    @Override
    public void setNodeOffline(String nodeName) {
        try {
            // 构造请求体，使用 transient 设置 exclude IP 或名称
            JSONObject requestBody = new JSONObject();
            JSONObject transientSettings = new JSONObject();
            transientSettings.put("cluster.routing.allocation.exclude._ip", nodeName); // 可改为 _name 等
            requestBody.put("transient", transientSettings);

            StringEntity entity = new StringEntity(
                    requestBody.toJSONString(),
                    ContentType.APPLICATION_JSON
            );

            String response = elasticClientService.executePutApi("/_cluster/settings", entity);
            log.info("节点 {} 下线成功，响应: {}", nodeName, response);

        } catch (Exception e) {
            log.error("设置节点下线失败: {}", nodeName, e);
            throw new RuntimeException("设置节点下线失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelNodeOffline(String nodeName) {
        try {
            JSONObject requestBody = new JSONObject();
            JSONObject transientSettings = new JSONObject();
            transientSettings.put("cluster.routing.allocation.exclude._ip", ""); // 清空
            requestBody.put("transient", transientSettings);

            StringEntity entity = new StringEntity(
                    requestBody.toJSONString(),
                    ContentType.APPLICATION_JSON
            );

            String response = elasticClientService.executePutApi("/_cluster/settings", entity);
            log.info("取消节点 {} 下线成功，响应: {}", nodeName, response);

        } catch (Exception e) {
            log.error("取消节点下线失败: {}", nodeName, e);
            throw new RuntimeException("取消节点下线失败: " + e.getMessage(), e);
        }
    }

}
