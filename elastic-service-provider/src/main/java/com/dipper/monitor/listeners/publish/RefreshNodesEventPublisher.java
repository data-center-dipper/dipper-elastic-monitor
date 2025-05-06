package com.dipper.monitor.listeners.publish;

import com.dipper.monitor.listeners.event.RefreshNodesEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class RefreshNodesEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishCustomEvent(final String message) {
        System.out.println("Publishing custom event. Message: " + message);
        RefreshNodesEvent customSpringEvent = new RefreshNodesEvent(this, message);
        applicationEventPublisher.publishEvent(customSpringEvent);
    }
}
