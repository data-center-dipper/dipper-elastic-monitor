package com.dipper.monitor.listeners.event;


import org.springframework.context.ApplicationEvent;

public class RefreshNodesEvent extends ApplicationEvent {
    private String message;

    public RefreshNodesEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}