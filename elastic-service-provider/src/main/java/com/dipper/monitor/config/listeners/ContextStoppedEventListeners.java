package com.dipper.monitor.config.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContextStoppedEventListeners   implements ApplicationListener<ContextStoppedEvent> {

    public ContextStoppedEventListeners() {

    }

    // 这个也没有测试到
    @Override
    public void onApplicationEvent(ContextStoppedEvent event) {
        log.info("接收到ContextStoppedEvent事件。。。");
    }
}
