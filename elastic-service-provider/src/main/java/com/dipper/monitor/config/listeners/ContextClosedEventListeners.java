package com.dipper.monitor.config.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContextClosedEventListeners  implements ApplicationListener<ContextClosedEvent> {

    public ContextClosedEventListeners() {
    }

    // 关闭服务的时候  能收到这个方法 接收到ContextClosedEvent事件。。。
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("接收到ContextClosedEvent事件。。。");
    }
}
