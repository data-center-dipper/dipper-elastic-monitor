package com.dipper.monitor.comment.storage.impl;

import com.dipper.monitor.entity.task.TaskMetadataEntity;
import com.dipper.monitor.comment.storage.MetadataStorage;
import com.dipper.monitor.mapper.TaskMetadataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 本地MySQL存储实现
 */
@Slf4j
@Service("localMySQLStorage")
public class LocalMySQLStorage implements MetadataStorage {

    @Autowired
    private TaskMetadataMapper taskMetadataMapper;

    @Override
    public int saveMetadata(List<TaskMetadataEntity> metadataList) {
        if (metadataList == null || metadataList.isEmpty()) {
            log.warn("没有元数据需要保存");
            return 0;
        }
        
        try {
            // 先删除相同注解类型的旧记录
            String annotationType = metadataList.get(0).getAnnotationType();
            taskMetadataMapper.deleteByAnnotationType(annotationType);
            
            // 批量插入新记录
            int count = taskMetadataMapper.batchInsert(metadataList);
            log.info("成功保存 {} 条注解元数据到本地MySQL", count);
            return count;
        } catch (Exception e) {
            log.error("保存注解元数据到本地MySQL时发生错误: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public List<TaskMetadataEntity> findByAnnotationType(String annotationType) {
        return taskMetadataMapper.findByAnnotationType(annotationType);
    }

    @Override
    public List<TaskMetadataEntity> findAll() {
        return taskMetadataMapper.findAll();
    }
}