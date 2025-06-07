package com.dipper.monitor.service.schedule.impl;

import com.dipper.monitor.entity.task.TaskMetadataEntity;
import com.dipper.monitor.enums.elastic.TaskStatusEnum;
import com.dipper.monitor.mapper.TaskMetadataMapper;
import com.dipper.monitor.service.schedule.MetadataStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
            List<TaskMetadataEntity> insertIndex = new ArrayList<>();;

            for (TaskMetadataEntity item:metadataList){
                String author = item.getAuthor();
                String annotationType = item.getAnnotationType();
                String taskName = item.getTaskName();
                String groupName = item.getGroupName();

                TaskMetadataEntity byUnique = taskMetadataMapper.findByUnique(author, annotationType, taskName, groupName);
                if(byUnique != null){
                    continue;
                }else{
                    insertIndex.add(item);
                }
            }

            for (TaskMetadataEntity item:insertIndex){
                item.setStatus(TaskStatusEnum.RUNNING.getStatus());
            }

            if(insertIndex == null || insertIndex.isEmpty()){
                log.warn("没有元数据需要保存");
                return 0;
            }

            // 批量插入新记录
            int count = taskMetadataMapper.batchInsert(insertIndex);
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