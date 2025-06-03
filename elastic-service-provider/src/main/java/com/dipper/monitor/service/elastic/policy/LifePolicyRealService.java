package com.dipper.monitor.service.elastic.policy;

import com.dipper.monitor.entity.elastic.policy.PolicyPageRequest;
import com.dipper.monitor.entity.elastic.policy.response.LifePolicyResponse;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
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


    Tuple2<List<LifePolicyResponse>, Long> getRealPolicies(PolicyPageRequest request);

    boolean deletePolicy(String policyName);
}
