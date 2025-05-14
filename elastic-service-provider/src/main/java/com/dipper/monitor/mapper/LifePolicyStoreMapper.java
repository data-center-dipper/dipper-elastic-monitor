package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.db.elastic.LifePolicyEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 生命周期策略数据库交互接口
 */
public interface LifePolicyStoreMapper {

    /**
     * 插入新策略
     */
    int insert(@Param("entity") LifePolicyEntity entity);

    /**
     * 更新策略
     */
    int update(@Param("entity") LifePolicyEntity entity);

    /**
     * 根据ID删除策略
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 根据ID查询策略
     */
    LifePolicyEntity selectById(@Param("id") Integer id);

    /**
     * 分页查询策略列表
     */
    List<LifePolicyEntity> selectByPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 查询符合条件的策略总数
     */
    long countByCondition(@Param("keyword") String keyword);

    /**
     * 检查英文名称是否已存在
     */
    int checkEnNameExists(@Param("clusterCode") String clusterCode,
                          @Param("enName") String enName,
                          @Param("excludeId") Integer excludeId);

    List<LifePolicyEntity> getAllPolicies();
}