package com.dipper.monitor.service.elastic.policy;

import com.dipper.monitor.entity.db.elastic.LifePolicyEntity;
import com.dipper.monitor.entity.elastic.policy.LifePolicyRequest;
import com.dipper.monitor.entity.elastic.policy.PolicyPageRequest;
import com.dipper.monitor.entity.elastic.policy.response.LifePolicyResponse;
import com.dipper.monitor.utils.Tuple2;

import java.util.List;

/**
 * 策略存储与定义
 */
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

    List<LifePolicyResponse> getAllPolicies();

    boolean batchInsertTemplates(List<LifePolicyEntity> toBeSaved);

    List<LifePolicyEntity> getAllPolicieEntitys();
}
