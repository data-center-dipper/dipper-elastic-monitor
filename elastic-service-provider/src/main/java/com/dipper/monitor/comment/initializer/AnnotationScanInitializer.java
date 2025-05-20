package com.dipper.monitor.comment.initializer;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.comment.service.AnnotationScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 注解扫描初始化器
 * 在应用启动完成后自动扫描并保存注解元数据
 */
@Slf4j
@Component
public class AnnotationScanInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private AnnotationScanService annotationScanService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("应用启动完成，开始扫描注解元数据...");
        
        try {
            // 扫描QuartzJob注解并保存元数据
            int count = annotationScanService.scanAndSave(QuartzJob.class, "com.dipper.monitor");
            log.info("成功扫描并保存 {} 条QuartzJob注解元数据", count);
            
            // 如果有其他需要扫描的注解，可以在这里添加
            // annotationScanService.scanAndSave(OtherAnnotation.class, "com.dipper.monitor");
            
        } catch (Exception e) {
            log.error("扫描注解元数据时发生错误: {}", e.getMessage(), e);
        }
        
        log.info("注解元数据扫描完成");
    }
}