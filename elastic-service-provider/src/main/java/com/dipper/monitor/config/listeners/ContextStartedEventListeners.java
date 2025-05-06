package com.dipper.monitor.config.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContextStartedEventListeners  implements ApplicationListener<ContextStartedEvent> {

    public ContextStartedEventListeners() {
    }

    // 没测试出来什么时候执行
    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        log.info("接收到ContextStartedEvent事件。。。");
    }
}
