package com.dipper.monitor.annotation.quartz;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 【Spring】使用Quartz注解实现Spring Boot中的定时任务
 * https://blog.csdn.net/qq_21383435/article/details/146761953
 */
@Slf4j
@Component
public class QuartzJobRegistrar implements ApplicationListener<ApplicationReadyEvent> {

    private final ApplicationContext applicationContext;

    public QuartzJobRegistrar(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            registerJobsAndTriggers();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerJobsAndTriggers() throws SchedulerException {
        // 使用ClassPathScanningCandidateComponentProvider来扫描所有带有@QuartzJob注解的方法所在的类
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("com.dipper")) { // 修改为你的基础包名
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                Object bean = applicationContext.getBean(clazz);

                for (Method method : AopUtils.getTargetClass(bean).getDeclaredMethods()) {
                    if (method.isAnnotationPresent(QuartzJob .class)) {
                        QuartzJob quartzJob = method.getAnnotation(QuartzJob.class);
                        log.info("准备处理:{}",quartzJob);
                        String groupName = quartzJob.groupName();

                        MethodInvokingJobDetailFactoryBean jobDetailFactory = new MethodInvokingJobDetailFactoryBean();
                        jobDetailFactory.setTargetObject(bean);
                        jobDetailFactory.setTargetMethod(method.getName());
                        jobDetailFactory.afterPropertiesSet();

                        JobDetail jobDetail = (JobDetail) jobDetailFactory.getObject();

                        Trigger trigger;
                        if (!quartzJob.cron().isEmpty()) {
                            trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(method.getName(), groupName)
                                    .withSchedule(CronScheduleBuilder.cronSchedule(quartzJob.cron()))
                                    .build();
                        } else if (quartzJob.fixedRate() > 0) {
                            trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(method.getName(), groupName)
                                    .startNow()
                                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                            .withIntervalInMilliseconds(quartzJob.fixedRate())
                                            .repeatForever())
                                    .build();
                        } else if (quartzJob.fixedDelay() > 0) {
                            // 注意: Quartz不直接支持fixedDelay模式，这里简化处理为fixedRate
                            trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(method.getName(), groupName)
                                    .startNow()
                                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                            .withIntervalInMilliseconds(quartzJob.fixedDelay())
                                            .repeatForever())
                                    .build();
                        } else {
                            throw new IllegalArgumentException("Either 'cron', 'fixedRate', or 'fixedDelay' must be specified.");
                        }

                        Scheduler scheduler = applicationContext.getBean(SchedulerFactoryBean.class).getScheduler();
                        scheduler.scheduleJob(jobDetail, trigger);
                    }
                }
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
}