package com.dipper.monitor.service.elastic.life.impl;

import com.alibaba.fastjson.JSON;
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
import org.apache.commons.lang3.StringUtils;
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

    @Override
    public List<EsLifeCycleManagement> getLifeCycleList() {
        try {
            String result = elasticClientService.executeGetApi(ElasticRestApi.LIFE_CYCLE_MANAGEMENT.getApiPath());
            if (StringUtils.isBlank(result) || result.contains("master_not_discovered_exception")) return Collections.emptyList();

            JSONObject jsonObject = JSON.parseObject(result).getJSONObject("indices");
            List<EsLifeCycleManagement> list = new ArrayList<>();
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                JSONObject value = (JSONObject) entry.getValue();
                if (!"false".equals(value.getString("managed")) && "ERROR".equalsIgnoreCase(value.getString("step"))) {
                    EsLifeCycleManagement management = new EsLifeCycleManagement();
                    management.setIndex(entry.getKey());
                    management.setMessage(value.toJSONString());
                    list.add(management);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("获取生命周期管理列表异常: {}", e.getMessage());
            return Collections.emptyList();
        }
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
