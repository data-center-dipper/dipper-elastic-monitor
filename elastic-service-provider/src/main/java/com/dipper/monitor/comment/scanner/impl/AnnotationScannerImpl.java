package com.dipper.monitor.comment.scanner.impl;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.entity.task.TaskMetadataEntity;
import com.dipper.monitor.comment.scanner.AnnotationScanner;
import com.dipper.monitor.mapper.TaskMetadataMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注解扫描器实现类
 */
@Slf4j
@Service
public class AnnotationScannerImpl implements AnnotationScanner {

    @Autowired
    private TaskMetadataMapper taskMetadataMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<TaskMetadataEntity> scanAnnotations(Class<? extends Annotation> annotationClass, String basePackage) {
        log.info("开始扫描注解: {}, 基础包: {}", annotationClass.getName(), basePackage);
        List<TaskMetadataEntity> metadataList = new ArrayList<>();
        
        try {
            // 使用ClassPathScanningCandidateComponentProvider扫描带有@Component注解的类
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));
            
            scanner.findCandidateComponents(basePackage).forEach(beanDefinition -> {
                try {
                    String className = beanDefinition.getBeanClassName();
                    Class<?> clazz = Class.forName(className);
                    
                    // 遍历类中的所有方法
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(annotationClass)) {
                            TaskMetadataEntity metadata = extractMetadata(annotationClass, method);
                            if (metadata != null) {
                                metadataList.add(metadata);
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    log.error("类加载失败: {}", e.getMessage(), e);
                } catch (Exception e) {
                    log.error("扫描注解时发生错误: {}", e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("扫描注解过程中发生异常: {}", e.getMessage(), e);
        }
        
        log.info("扫描完成，共找到 {} 个带有 {} 注解的方法", metadataList.size(), annotationClass.getName());
        return metadataList;
    }

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
            log.info("成功保存 {} 条注解元数据", count);
            return count;
        } catch (Exception e) {
            log.error("保存注解元数据时发生错误: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int scanAndSave(Class<? extends Annotation> annotationClass, String basePackage) {
        List<TaskMetadataEntity> metadataList = scanAnnotations(annotationClass, basePackage);
        return saveMetadata(metadataList);
    }
    
    /**
     * 提取方法上注解的元数据
     * 
     * @param annotationClass 注解类
     * @param method 方法
     * @return 注解元数据实体
     */
    private TaskMetadataEntity extractMetadata(Class<? extends Annotation> annotationClass, Method method) {
        try {
            Annotation annotation = method.getAnnotation(annotationClass);
            if (annotation == null) {
                return null;
            }
            
            TaskMetadataEntity metadata = new TaskMetadataEntity();
            metadata.setAnnotationType(annotationClass.getName());
            metadata.setClassName(method.getDeclaringClass().getName());
            metadata.setMethodName(method.getName());
            
            // 处理QuartzJob注解
            if (annotationClass == QuartzJob.class) {
                QuartzJob quartzJob = (QuartzJob) annotation;
                metadata.setCron(quartzJob.cron());
                metadata.setFixedRate(quartzJob.fixedRate() == -1L ? null : quartzJob.fixedRate());
                metadata.setFixedDelay(quartzJob.fixedDelay() == -1L ? null : quartzJob.fixedDelay());
                metadata.setAuthor(quartzJob.author());
                metadata.setGroupName(quartzJob.groupName());
                metadata.setJobDesc(quartzJob.jobDesc());
                metadata.setEditAble(quartzJob.editAble());
            } else {
                // 对于其他类型的注解，使用反射提取所有属性
                Map<String, Object> attributes = new HashMap<>();
                for (Method attributeMethod : annotationClass.getDeclaredMethods()) {
                    if (attributeMethod.getParameterCount() == 0) {
                        String name = attributeMethod.getName();
                        Object value = attributeMethod.invoke(annotation);
                        attributes.put(name, value);
                        
                        // 尝试设置通用字段
                        if ("cron".equals(name) && value instanceof String) {
                            metadata.setCron((String) value);
                        } else if ("fixedRate".equals(name) && value instanceof Long) {
                            Long val = (Long) value;
                            metadata.setFixedRate(val == -1L ? null : val);
                        } else if ("fixedDelay".equals(name) && value instanceof Long) {
                            Long val = (Long) value;
                            metadata.setFixedDelay(val == -1L ? null : val);
                        } else if ("author".equals(name) && value instanceof String) {
                            metadata.setAuthor((String) value);
                        } else if ("groupName".equals(name) && value instanceof String) {
                            metadata.setGroupName((String) value);
                        } else if ("jobDesc".equals(name) && value instanceof String) {
                            metadata.setJobDesc((String) value);
                        } else if ("editAble".equals(name) && value instanceof Boolean) {
                            metadata.setEditAble((Boolean) value);
                        }
                    }
                }
                
                // 将其他属性序列化为JSON存储
                metadata.setAdditionalAttributes(objectMapper.writeValueAsString(attributes));
            }
            
            return metadata;
        } catch (Exception e) {
            log.error("提取注解元数据时发生错误: {}", e.getMessage(), e);
            return null;
        }
    }
}