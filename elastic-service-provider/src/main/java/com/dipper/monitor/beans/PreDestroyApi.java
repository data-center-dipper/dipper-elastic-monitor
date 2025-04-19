package com.dipper.monitor.beans;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/***
 * todo: 九师兄  2023/5/7 22:15
 *
 * 【Spring】Spring boot @PostConstruct & @PreDestroy
 * https://blog.csdn.net/qq_21383435/article/details/100514071
 */
@Configuration
@Slf4j
public class PreDestroyApi implements InitializingBean, DisposableBean {

    public PreDestroyApi() {
        log.info("构造方法被调用");
    }

    @Override
    public void afterPropertiesSet() {
        log.info("afterPropertiesSet方法被调用");
    }

    @Override
    public void destroy() {
        log.info("destroy方法被调用");
    }

}