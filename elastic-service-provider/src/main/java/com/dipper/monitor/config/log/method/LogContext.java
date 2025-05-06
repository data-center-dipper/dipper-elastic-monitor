package com.dipper.monitor.config.log.method;

import java.util.ArrayList;
import java.util.List;

public class LogContext {
    private List<String> logs = new ArrayList<>();

    public void addLog(String log) {
        this.logs.add(log);
    }

    public List<String> getLogs() {
        return logs;
    }
}