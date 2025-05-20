package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.task.TaskMetadataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 注解元数据Mapper接口
 */
@Mapper
public interface AnnotationMetadataMapper {
    
    /**
     * 根据注解类型删除元数据
     * 
     * @param annotationType 注解类型名称
     * @return 删除的记录数
     */
    int deleteByAnnotationType(@Param("annotationType") String annotationType);
    
    /**
     * 批量插入注解元数据
     * 
     * @param metadataList 注解元数据列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("metadataList") List<TaskMetadataEntity> metadataList);
    
    /**
     * 根据注解类型查询元数据
     * 
     * @param annotationType 注解类型名称
     * @return 注解元数据列表
     */
    List<TaskMetadataEntity> findByAnnotationType(@Param("annotationType") String annotationType);
    
    /**
     * 查询所有注解元数据
     * 
     * @return 注解元数据列表
     */
    List<TaskMetadataEntity> findAll();

    Long countTasks(String keyword);

    List<TaskMetadataEntity> findTasksByPage(String keyword, Integer pageSize, int offset);

    TaskMetadataEntity findById(Integer taskId);
}
