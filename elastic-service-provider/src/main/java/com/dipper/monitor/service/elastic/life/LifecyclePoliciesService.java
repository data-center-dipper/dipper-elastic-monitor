package com.dipper.monitor.service.elastic.life;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;

import java.util.List;
import java.util.Map;

public interface LifecyclePoliciesService {

    List<JSONObject> getLifeCycleList();


}
