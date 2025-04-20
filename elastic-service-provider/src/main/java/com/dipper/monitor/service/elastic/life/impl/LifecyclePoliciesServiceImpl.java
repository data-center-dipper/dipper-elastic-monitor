package com.dipper.monitor.service.elastic.life.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.client.proxy.params.elasticsearch.Request;
import com.dipper.monitor.annotation.log.CollectLogs;
import com.dipper.monitor.config.log.method.ResultWithLogs;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.life.LifecyclePoliciesService;
import com.dipper.monitor.service.elastic.life.impl.service.CheckLifeCycleErrorService;
import com.dipper.monitor.service.elastic.life.impl.service.RepairLifeCycleErrorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.StringEntity;
import org.checkerframework.checker.units.qual.A;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class LifecyclePoliciesServiceImpl implements LifecyclePoliciesService {

    @Autowired
    private ElasticClientService elasticClientService;
//    @Autowired
//    @Lazy
//    private RepairLifeCycleErrorService repairLifeCycleErrorService;

//    @CollectLogs // 自定义注解，用于标记需要收集日志的方法
    public List<JSONObject> getLifeCycleList() {
        try {
            // 创建请求并设置空请求体
            String ilmExplainResult = elasticClientService.executeGetApi("/*/_ilm/explain?pretty");
            log.info("索引生命周期管理状态：{}", ilmExplainResult);

           List<JSONObject> result = new ArrayList<>();
            JSONObject jsonObject = JSONObject.parseObject(ilmExplainResult);
            JSONObject indices = jsonObject.getJSONObject("indices");
            for (String index : indices.keySet()) {
                JSONObject indexInfo = indices.getJSONObject(index);
                String step = indexInfo.getString("step");
                Boolean managed = indexInfo.getBoolean("managed");
              if ("false".equals(managed)) {
                    continue;
                   }
               if (!"ERROR".equalsIgnoreCase(step)) {
                   continue;
                }
                result.add(indexInfo);
            }
            return result;
        } catch (Exception e) {
            log.error("检查ILM问题时发生错误", e);
        }
        return Collections.emptyList();
    }

    @Override
    public String checkLifeCycleError() throws IOException {
        CheckLifeCycleErrorService checkLifeCycleErrorService = new CheckLifeCycleErrorService();
        return checkLifeCycleErrorService.checkLifeCycleError();
    }

    @Override
    public String repairLifeCycleError() throws IOException {
//        boolean cglibProxy = AopUtils.isCglibProxy(repairLifeCycleErrorService);
//        log.info("cglibProxy:{}",cglibProxy);
        RepairLifeCycleErrorService repairLifeCycleErrorService = new RepairLifeCycleErrorService();
        return repairLifeCycleErrorService.repairLifeCycleError();
    }

    @Override
    public String openLifeCycle() {
        String result = null;
        try {
            result = this.elasticClientService.executePostApi(ElasticRestApi.LIFE_CYCLE_START.getApiPath(), null);
        } catch (IOException e) {
            log.info("开启生命周期失败:{}", e.getMessage(), e);
        }
        return result;
    }
}
