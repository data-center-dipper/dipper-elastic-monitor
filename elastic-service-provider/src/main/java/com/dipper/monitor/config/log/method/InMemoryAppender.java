package com.dipper.monitor.config.log.method;

import ch.qos.logback.core.AppenderBase;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAppender extends AppenderBase<ch.qos.logback.classic.spi.ILoggingEvent> {
    private List<String> logs = new ArrayList<>();

    public List<String> getLogs() {
        return logs;
    }

    @Override
    protected void append(ch.qos.logback.classic.spi.ILoggingEvent eventObject) {
        logs.add(eventObject.getFormattedMessage());
    }
}