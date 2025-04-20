package com.dipper.monitor.service.elastic.life.impl.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.dipper.monitor.enums.elastic.ElasticRestApi;

import java.io.IOException;
import java.util.List;

public class CheckLifeCycleErrorService extends AbstractLifeCycleError {


    public String checkLifeCycleError() throws IOException {
        List<JSONObject> list = lifecyclePoliciesService.getLifeCycleList();
        StringBuilder builder = new StringBuilder();

        if (!list.isEmpty()) {
            String clusterSetting = elasticClientService.executeGetApi(ElasticRestApi.CLUSTER_SETTING.getApiPath());
            JSONObject clusteringSettingJson = JSON.parseObject(clusterSetting);
            String pollInterval = (String) JSONPath.eval(clusteringSettingJson, "$.indices.lifecycle.poll_interval");

            String lifeStatus =  elasticClientService.executeGetApi(ElasticRestApi.LIFE_CYCLE_STATUE.getApiPath());
            builder.append("集群分析\r\n");
            builder.append("transient.indices.lifecycle.poll_interval:\t").append(pollInterval).append("\r\r");
            builder.append("生命周期运行状态:\r\n").append(lifeStatus).append("\r\n");

            builder.append("-----------------------------------------------\n\r");
            builder.append("异常的生命周期:\r\n");

            builder.append(JSON.toJSONString(list));
        } else {
            builder.append("没有异常的生命周期");
        }
        return builder.toString();
    }


}
