package com.dipper.monitor.annotation.quartz;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuartzJob {
    String cron() default ""; // 支持cron表达式
    long fixedRate() default -1L; // 固定速率执行
    long fixedDelay() default -1L; // 固定延迟执行
    String author();
    String groupName();
    String jobDesc();
    boolean editAble() default false;
}