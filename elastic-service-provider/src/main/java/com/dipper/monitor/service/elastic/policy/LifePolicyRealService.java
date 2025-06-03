package com.dipper.monitor.service.elastic.policy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 策略与es集群交互相关
 */
public interface LifePolicyRealService {
    /**
     * 让所有的策略都生效
     */
    void policyAllRefresh();
    /**
     * 让某个策略实时生效
     * @param id
     */
    void policyEffective(Integer id) throws IOException;

    /**
     * 获取已经生效的策略
     */
    Map<String,String> policyList() throws UnsupportedEncodingException, IOException;


}
