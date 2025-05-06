package com.dipper.monitor.annotation.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 确保添加了正确的元注解
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) // 指定该注解只能用于方法
public @interface CollectLogs {
}