package com.dipper.monitor.aware;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;


/***
 * todo: 九师兄  2024/3/29 13:31
 *
 * 【Spring】Spring事件是如何运转的
 * https://blog.csdn.net/qq_21383435/article/details/137134376
 */
@Slf4j
@Component
public class UserApplicationEventPublisherAware implements ApplicationEventPublisherAware  {

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        log.info("设置applicationEventPublisher");
    }

    @PostConstruct
    public void init(){
        log.info("初始化applicationEventPublisher");
        applicationEventPublisher.publishEvent("发布服务成功事件");
    }
}
