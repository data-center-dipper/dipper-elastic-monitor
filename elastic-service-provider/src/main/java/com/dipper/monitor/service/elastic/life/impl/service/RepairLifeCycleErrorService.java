package com.dipper.monitor.service.elastic.life.impl.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.alians.ElasticAliansService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.life.LifecyclePoliciesService;
import com.dipper.monitor.service.elastic.shard.ShardService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
public class RepairLifeCycleErrorService {

    @Autowired
    private LifecyclePoliciesService lifecyclePoliciesService;
    @Autowired
    private ShardService shardService;
    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticAliansService elasticAliansService;

    private StringBuilder builder = new StringBuilder(3000);

    public RepairLifeCycleErrorService(){
        lifecyclePoliciesService = SpringUtil.getBean(LifecyclePoliciesService.class);
        shardService = SpringUtil.getBean(ShardService.class);
        elasticClientService = SpringUtil.getBean(ElasticClientService.class);
        elasticAliansService = SpringUtil.getBean(ElasticAliansService.class);
    }

    public String repairLifeCycleError() throws IOException {
        List<JSONObject> list = lifecyclePoliciesService.getLifeCycleList();
        if (list.isEmpty()) {
            builder.append("未有异常的生命周期\n");
            return builder.toString();
        }

        String clusterSetting = elasticClientService.executeGetApi(ElasticRestApi.CLUSTER_SETTING.getApiPath());
        JSONObject clusteringSettingJson = JSON.parseObject(clusterSetting);
        String pollInterval = (String) JSONPath.eval(clusteringSettingJson, "$.indices.lifecycle.poll_interval");

        String lifeStatus = elasticClientService.executeGetApi(ElasticRestApi.LIFE_CYCLE_STATUE.getApiPath());

        builder.append("准备进行集群分析\r\n")
                .append("transient.indices.lifecycle.poll_interval:\t").append(pollInterval).append("\r\n")
                .append("当前生命周期运行状态:\n").append(lifeStatus).append("\r\n");
        if ("STOP".equalsIgnoreCase(lifeStatus)) {
            builder.append("集群的生命周期处于停止状态.. 现在执行开启：").append(ElasticRestApi.LIFE_CYCLE_START.getApiPath()).append("\r\n");
            String result = lifecyclePoliciesService.openLifeCycle();
            builder.append("执行结果:\n").append(result).append("\r\n");
        }

        solveEveryOne(list, builder);

        builder.append("-----------------------------------------------\r\n")
                .append("重试生命周期异常的索引:\r\n");
        for (JSONObject esLifeCycleManagement : list) {
            String index = esLifeCycleManagement.getString("index");
            String api = ElasticRestApi.LIFE_CYCLE_RETRY.getApiPath().replace("{prefix}", index);
            try {
                String result = elasticClientService.executePostApi(api, null);
                builder.append("索引:").append(index).append("\n执行结果:\n").append(result).append("\r\n");
            } catch (IOException e) {
                builder.append("重试生命周期失败: ").append(e.getMessage()).append("\r\n");
            }
        }
        return builder.toString();
    }

    private void solveEveryOne(List<JSONObject> list, StringBuilder builder) {
        for (JSONObject life : list) {
            String index = life.getString("index");
            String failedStep = life.getString("failed_step");
            if ("check-rollover-ready".equalsIgnoreCase(failedStep)) {
                lifeCheckRolloverReadySolve(builder, life, index);
            }
        }
    }

    private void lifeCheckRolloverReadySolve(StringBuilder builder, JSONObject obj, String index) {
        String type = (String) JSONPath.eval(obj, "$.step_info.type");
        String reason = (String) JSONPath.eval(obj, "$.step_info.reason");
        if (type.equalsIgnoreCase("illegal_argument_exception") && reason.contains("is not the write index for alias")) {
            indexAliansCanNotWriteSolve(builder, reason);
        }
    }

    private void indexAliansCanNotWriteSolve(StringBuilder builder, String reason) {
        String[] data = reason.split("]");
        String indexAndOther = data[0];
        if (StringUtils.isBlank(indexAndOther)) {
            return;
        }
        int leftPosition = indexAndOther.indexOf("[");
        if (leftPosition < 0) {
            leftPosition = 0;
        }
        String alians = indexAndOther.substring(leftPosition + 1);
        String aliansData = null;
        try {
            aliansData = elasticClientService.executeGetApi(alians + "/_alias");
        } catch (IOException e) {
            builder.append("获取别名信息出错: ").append(e.getMessage()).append("\r\n");
            return;
        }
        if (!elasticAliansService.isWriteEx(aliansData)) {
            return;
        }
        String indexMax = elasticAliansService.getAliansMaxIndexRolling(aliansData);
        if (StringUtils.isBlank(indexMax)) {
            return;
        }

        builder.append("添加索引：").append(indexMax).append(" 别名：").append(alians).append(" 设置可写状态\r\n");
        try {
            elasticAliansService.changeIndexWrite(indexMax, alians, true);
        } catch (Exception e) {
            builder.append("更改索引为可写状态失败：").append(e.getMessage().substring(0, 300)).append("\r\n");
        }
    }
}