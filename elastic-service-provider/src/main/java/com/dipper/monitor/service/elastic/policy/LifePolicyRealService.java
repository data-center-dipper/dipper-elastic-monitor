package com.dipper.monitor.service.elastic.policy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 策略与es集群交互相关
 */
public interface LifePolicyRealService {
    /**
     * 让某个策略实时生效
     * @param id
     */
    void policyEffective(Integer id) throws IOException;
}
