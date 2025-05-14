package com.dipper.monitor.service.elastic.policy.impl;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.policy.LifePolicyRequest;
import com.dipper.monitor.entity.elastic.policy.PolicyPageRequest;
import com.dipper.monitor.entity.elastic.policy.response.LifePolicyResponse;
import com.dipper.monitor.entity.db.elastic.LifePolicyEntity;
import com.dipper.monitor.mapper.LifePolicyStoreMapper;
import com.dipper.monitor.service.elastic.policy.LifePolicyRealService;
import com.dipper.monitor.service.elastic.policy.LifePolicyStoreService;
import com.dipper.monitor.utils.DipperDateUtil;
import com.dipper.monitor.utils.Tuple2;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LifePolicyStoreServiceImpl implements LifePolicyStoreService {

    @Autowired
    private LifePolicyStoreMapper lifePolicyStoreMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LifePolicyResponse addPolicy(LifePolicyRequest request) {
        // 参数校验
        if (!StringUtils.hasText(request.getZhName())) {
            throw new IllegalArgumentException("中文名称不能为空");
        }
        if (!StringUtils.hasText(request.getEnName())) {
            throw new IllegalArgumentException("英文名称不能为空");
        }
        if (!StringUtils.hasText(request.getPolicyContent())) {
            throw new IllegalArgumentException("策略内容不能为空");
        }
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();

        // 检查英文名称是否已存在
        if (lifePolicyStoreMapper.checkEnNameExists(clusterCode,request.getEnName(), -1) > 0) {
            throw new IllegalArgumentException("英文名称已存在");
        }
        
        // 转换为实体对象
        LifePolicyEntity entity = new LifePolicyEntity();
        entity.setClusterCode(clusterCode);
        entity.setZhName(request.getZhName());
        entity.setEnName(request.getEnName());
        entity.setPolicyValue(request.getPolicyContent());
        
        // 保存到数据库
        lifePolicyStoreMapper.insert(entity);
        
        // 返回结果
        return convertToResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LifePolicyResponse updatePolicy(LifePolicyRequest request) {
        // 参数校验
        if (request.getId() == null) {
            throw new IllegalArgumentException("策略ID不能为空");
        }
        if (!StringUtils.hasText(request.getZhName())) {
            throw new IllegalArgumentException("中文名称不能为空");
        }
        if (!StringUtils.hasText(request.getEnName())) {
            throw new IllegalArgumentException("英文名称不能为空");
        }
        if (!StringUtils.hasText(request.getPolicyContent())) {
            throw new IllegalArgumentException("策略内容不能为空");
        }

        // 检查策略是否存在
        LifePolicyEntity existingEntity = lifePolicyStoreMapper.selectById(request.getId());
        if (existingEntity == null) {
            throw new IllegalArgumentException("策略不存在");
        }

        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();

        // 检查英文名称是否已存在（排除自身）
        if (lifePolicyStoreMapper.checkEnNameExists(clusterCode,request.getEnName(), request.getId()) > 0) {
            throw new IllegalArgumentException("英文名称已存在");
        }
        
        // 更新实体对象
        existingEntity.setZhName(request.getZhName());
        existingEntity.setEnName(request.getEnName());
        existingEntity.setPolicyValue(request.getPolicyContent());
        
        // 更新数据库
        lifePolicyStoreMapper.update(existingEntity);
        
        // 返回结果
        return convertToResponse(existingEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePolicy(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("策略ID不能为空");
        }
        
        // 检查策略是否存在
        LifePolicyEntity existingEntity = lifePolicyStoreMapper.selectById(id);
        if (existingEntity == null) {
            throw new IllegalArgumentException("策略不存在");
        }
        
        // 删除策略
        int result = lifePolicyStoreMapper.deleteById(id);
        return result > 0;
    }

    @Override
    public LifePolicyResponse getOnePolicy(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("策略ID不能为空");
        }
        
        // 查询策略
        LifePolicyEntity entity = lifePolicyStoreMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        
        // 返回结果
        return convertToResponse(entity);
    }

    @Override
    public Tuple2<List<LifePolicyResponse>,Long> getPoliciesByPage(PolicyPageRequest request) {
        if (request == null) {
            request = new PolicyPageRequest();
        }
        
        // 计算分页参数
        int pageNum = Math.max(request.getPageNum(), 1);
        int pageSize = Math.max(request.getPageSize(), 1);
        int offset = (pageNum - 1) * pageSize;
        
        // 查询数据
        List<LifePolicyEntity> entities = lifePolicyStoreMapper.selectByPage(
                request.getKeyword(), offset, pageSize);
        
        // 查询总数
        long total = lifePolicyStoreMapper.countByCondition(request.getKeyword());
        
        // 转换为响应对象
        List<LifePolicyResponse> responses = entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        LifePolicyRealService lifePolicyRealService = SpringUtil.getBean(LifePolicyRealService.class);
        Map<String, String> relMap = new HashMap<>();
        try {
            relMap = lifePolicyRealService.policyList();
        } catch (IOException e) {
            log.error("获取策略失败", e);
        }
        for (LifePolicyResponse response : responses) {
            String realPolicy = relMap.get(response.getEnName());
            if(org.apache.commons.lang3.StringUtils.isBlank(realPolicy)){
                response.setEffectStatus("未生效");
            }else {
                response.setEffectStatus("生效中");
            }
        }

        // 返回分页结果
        return Tuple2.of(responses, total);
    }

    @Override
    public List<LifePolicyResponse> getAllPolicies() {
        List<LifePolicyEntity> entitys = lifePolicyStoreMapper.getAllPolicies();
        if(CollectionUtils.isEmpty(entitys)){
            return new ArrayList<>();
        }
        List<LifePolicyResponse> lifePolicyResponses = new ArrayList<>();
        for (LifePolicyEntity entity : entitys) {
            LifePolicyResponse lifePolicyResponse = convertToResponse(entity);
            lifePolicyResponses.add(lifePolicyResponse);
        }
        return lifePolicyResponses;
    }

    /**
     * 将实体对象转换为响应对象
     */
    private LifePolicyResponse convertToResponse(LifePolicyEntity entity) {
        if (entity == null) {
            return null;
        }
        
        LifePolicyResponse response = new LifePolicyResponse();
        response.setId(entity.getId());
        response.setClusterCode(entity.getClusterCode());
        response.setZhName(entity.getZhName());
        response.setEnName(entity.getEnName());
        response.setPolicyContent(entity.getPolicyValue());
        response.setUpdateTime(DipperDateUtil.formatDate(entity.getUpdateTime()));
        
        return response;
    }
}
