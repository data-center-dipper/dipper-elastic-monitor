package com.dipper.monitor.comment.service.impl;

import com.dipper.monitor.entity.task.TaskMetadataEntity;
import com.dipper.monitor.comment.scanner.AnnotationScanner;
import com.dipper.monitor.comment.service.AnnotationScanService;
import com.dipper.monitor.comment.storage.MetadataStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * 注解扫描服务实现类
 */
@Slf4j
@Service
public class AnnotationScanServiceImpl implements AnnotationScanService {

    @Autowired
    private AnnotationScanner annotationScanner;
    
    @Autowired
    @Qualifier("localMySQLStorage")
    private MetadataStorage localMySQLStorage;
    
    @Autowired
    @Qualifier("remoteRESTStorage")
    private MetadataStorage remoteRESTStorage;

    @Override
    public int scanAndSave(Class<? extends Annotation> annotationClass, String basePackage) {
        // 默认使用本地MySQL存储
        return scanAndSave(annotationClass, basePackage, "local");
    }

    @Override
    public int scanAndSave(Class<? extends Annotation> annotationClass, String basePackage, String storageType) {
        log.info("开始扫描注解: {}, 基础包: {}, 存储类型: {}", annotationClass.getName(), basePackage, storageType);
        
        try {
            // 使用注解扫描器扫描注解
            List<TaskMetadataEntity> metadataList = annotationScanner.scanAnnotations(annotationClass, basePackage);
            
            if (metadataList == null || metadataList.isEmpty()) {
                log.warn("未找到带有 {} 注解的方法", annotationClass.getName());
                return 0;
            }
            
            // 根据存储类型选择存储实现
            MetadataStorage storage;
            if ("remote".equalsIgnoreCase(storageType)) {
                storage = remoteRESTStorage;
                log.info("使用远程REST存储");
            } else {
                storage = localMySQLStorage;
                log.info("使用本地MySQL存储");
            }
            
            // 保存元数据
            int count = storage.saveMetadata(metadataList);
            log.info("成功保存 {} 条注解元数据", count);
            return count;
        } catch (Exception e) {
            log.error("扫描并保存注解元数据时发生错误: {}", e.getMessage(), e);
            return 0;
        }
    }
}