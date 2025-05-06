package com.dipper.monitor.beans;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/***
 * todo: 九师兄  2023/4/29 21:44
 *
 * curl -X GET  "http://localhost:8082/beans/show?data=show"
 * 2e9a73d9-e3bd-431a-af84-ba4877fbe7b0
 * https://spring.hhui.top/spring-blog/2021/11/18/211118-SpringBoot%E7%B3%BB%E5%88%97%E4%B9%8BBean%E6%96%B9%E6%B3%95%E8%B0%83%E7%94%A8%E5%AF%BC%E8%87%B4NPE/
 */
@Component
public class SpringUtil implements ApplicationContextAware, EnvironmentAware {
    private static ApplicationContext applicationContext;
    private static Environment environment;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        SpringUtil.environment = environment;
    }

    public static <T> T getBean(Class<T> clz) {
        return applicationContext.getBean(clz);
    }

    public static String getProperty(String key) {
        return environment.getProperty(key);
    }
}