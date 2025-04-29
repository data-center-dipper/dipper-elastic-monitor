package com.dipper.monitor.service.elastic.life;

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
}
