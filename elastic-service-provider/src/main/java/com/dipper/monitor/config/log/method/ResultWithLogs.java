package com.dipper.monitor.config.log.method;

import java.util.List;

public class ResultWithLogs<T> {
    private T data;
    private List<String> logs;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}