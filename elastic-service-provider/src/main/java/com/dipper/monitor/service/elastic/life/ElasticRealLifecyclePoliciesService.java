package com.dipper.monitor.service.elastic.life;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;

import java.io.IOException;
import java.util.List;

public interface ElasticRealLifecyclePoliciesService {

    /**
     * 获取生命周期异常列表
     * @return
     */
    List<EsLifeCycleManagement> getLifeCycleList();
    /**
     * 获取生命周期异常列表
     * @return
     */
    List<JSONObject> getJsonLifeCycleList();

    /**
     * 检测生命周期异常
     * @return
     */
    String checkLifeCycleError() throws IOException;
    /**
     * 生命周期异常 修复
     * @return
     */
    String repairLifeCycleError() throws IOException;

    /**
     * 开启生命周期
     */
    String openLifeCycle();

    List<EsLifeCycleManagement> getLifeCycleExList(String indexXing) throws IOException;

    /**
     * 结束索引生命周期
     * @param indexName 索引名称
     * @return 操作结果
     * @throws IOException IO异常
     */
    String lifeCycleEnd(String indexName) throws IOException;
}
