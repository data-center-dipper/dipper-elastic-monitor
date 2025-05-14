package com.dipper.monitor.service.elastic.policy;

import com.dipper.monitor.dto.request.LifePolicyRequest;
import com.dipper.monitor.dto.request.PolicyPageRequest;
import com.dipper.monitor.dto.response.LifePolicyResponse;
import com.dipper.monitor.utils.Tuple2;

import java.util.List;

public interface LifePolicyStoreService {
    /**
     * 添加策略
     */
    LifePolicyResponse addPolicy(LifePolicyRequest request);
    
    /**
     * 更新策略
     */
    LifePolicyResponse updatePolicy(LifePolicyRequest request);
    
    /**
     * 删除策略
     */
    boolean deletePolicy(Integer id);
    
    /**
     * 获取单个策略详情
     */
    LifePolicyResponse getOnePolicy(Integer id);
    
    /**
     * 分页查询策略列表
     */
    Tuple2<List<LifePolicyResponse>,Long> getPoliciesByPage(PolicyPageRequest request);
}
