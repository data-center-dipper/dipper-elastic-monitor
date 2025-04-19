package com.dipper.monitor.service.elastic.life;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.config.log.method.ResultWithLogs;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface LifecyclePoliciesService {

    /**
     * 获取生命周期异常列表
     * @return
     */
    List<JSONObject> getLifeCycleList();

    /**
     * 生命周期异常 修复
     * @return
     */
    String repairLifeCycleError() throws IOException;

    /**
     * 开启生命周期
     */
    String openLifeCycle();
}
