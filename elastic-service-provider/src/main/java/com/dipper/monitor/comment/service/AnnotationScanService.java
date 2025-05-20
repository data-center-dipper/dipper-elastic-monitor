package com.dipper.monitor.comment.service;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * 注解扫描服务接口
 */
public interface AnnotationScanService {

    /**
     * 扫描并保存注解元数据到默认存储（本地MySQL）
     *
     * @param annotationClass 注解类
     * @param basePackage     基础包名
     * @return 保存成功的记录数
     */
    int scanAndSave(Class<? extends Annotation> annotationClass, String basePackage);

    /**
     * 扫描并保存注解元数据到指定存储
     *
     * @param annotationClass 注解类
     * @param basePackage     基础包名
     * @param storageType     存储类型（"local" 或 "remote"）
     * @return 保存成功的记录数
     */
    public int scanAndSave(Class<? extends Annotation> annotationClass, String basePackage, String storageType);

}