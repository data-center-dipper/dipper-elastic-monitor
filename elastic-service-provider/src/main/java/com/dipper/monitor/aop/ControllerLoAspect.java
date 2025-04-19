package com.dipper.monitor.aop;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;


@Component
@Aspect
public class ControllerLoAspect {

    private static final Logger logger = LoggerFactory.getLogger(ControllerLoAspect.class);

    @Pointcut("execution(public * com.dipper.monitor.controller..*.*(..))")
    public void log() {
    }

    @Before("log()")
    public void beforeLog(JoinPoint joinPoint) {
        String s = joinPoint.toString();
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return;
        HttpServletRequest request = requestAttributes.getRequest();

        logger.info("url :{}", request.getRequestURL().toString());
        logger.info("http_method :{}", request.getMethod());
        logger.info("IP :{}", request.getRemoteAddr());

        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            logger.info("参数名称:{} 参数值:{}", name, request.getParameter(name));
        }
    }

    @AfterReturning(returning = "ret", pointcut = "log()")
    public void doAfterReturning(Object ret) {
        if (ret != null && ret.toString().length() > 65535) {
            ret = ret.toString().substring(0, 65535);
            logger.info("返回值太长已经截取:{}", ret);
        } else {
            logger.info("返回值:{}", ret.toString());
        }
    }
}