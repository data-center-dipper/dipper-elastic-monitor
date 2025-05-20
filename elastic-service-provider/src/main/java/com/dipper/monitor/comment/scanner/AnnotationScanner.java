package com.dipper.monitor.comment.scanner;

import com.dipper.monitor.entity.task.TaskMetadataEntity;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * 注解扫描器接口
 */
public interface AnnotationScanner {

    /**
     * 扫描指定包下的所有类，查找带有指定注解的方法，并提取元数据
     * 
     * @param annotationClass 注解类
     * @param basePackage 基础包名
     * @return 注解元数据列表
     */
    List<TaskMetadataEntity> scanAnnotations(Class<? extends Annotation> annotationClass, String basePackage);
    
    /**
     * 将注解元数据保存到数据库
     * 
     * @param metadataList 注解元数据列表
     * @return 保存成功的记录数
     */
    int saveMetadata(List<TaskMetadataEntity> metadataList);
    
    /**
     * 扫描并保存注解元数据
     * 
     * @param annotationClass 注解类
     * @param basePackage 基础包名
     * @return 保存成功的记录数
     */
    int scanAndSave(Class<? extends Annotation> annotationClass, String basePackage);
}