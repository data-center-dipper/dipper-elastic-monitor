package com.dipper.monitor.comment.storage;

import com.dipper.monitor.entity.task.TaskMetadataEntity;

import java.util.List;

/**
 * 元数据存储接口
 */
public interface MetadataStorage {

    /**
     * 保存注解元数据
     * 
     * @param metadataList 注解元数据列表
     * @return 保存成功的记录数
     */
    int saveMetadata(List<TaskMetadataEntity> metadataList);
    
    /**
     * 根据注解类型查询元数据
     * 
     * @param annotationType 注解类型名称
     * @return 注解元数据列表
     */
    List<TaskMetadataEntity> findByAnnotationType(String annotationType);
    
    /**
     * 查询所有注解元数据
     * 
     * @return 注解元数据列表
     */
    List<TaskMetadataEntity> findAll();
}