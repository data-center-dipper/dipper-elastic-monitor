package com.dipper.monitor.aop.log;
import ch.qos.logback.classic.Logger;
import com.dipper.monitor.config.log.method.InMemoryAppender;
import com.dipper.monitor.config.log.method.ResultWithLogs;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(com.dipper.monitor.annotation.log.CollectLogs)")
    public Object collectMethodLogs(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取或创建自定义Appender实例
        String loggerName = joinPoint.getTarget().getClass().getName();
        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        InMemoryAppender inMemoryAppender = (InMemoryAppender) logger.getAppender("IN_MEMORY");

        if (inMemoryAppender == null) {
            // 如果Appender不存在，则创建并添加它
            inMemoryAppender = new InMemoryAppender();
            inMemoryAppender.setContext(logger.getLoggerContext());
            inMemoryAppender.start();
            logger.addAppender(inMemoryAppender);
        } else {
            // 清空之前可能存在的日志
            inMemoryAppender.getLogs().clear();
        }

        Object result = null;
        try {
            result = joinPoint.proceed(); // Proceed to the actual method call
        } finally {
            // 确保即使发生异常也能收集日志
            List<String> collectedLogs = inMemoryAppender.getLogs();
            if(result instanceof ResultWithLogs) {
                ((ResultWithLogs<?>) result).setLogs(collectedLogs);
            }
        }

        return result;
    }
}