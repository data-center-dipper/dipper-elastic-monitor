package com.dipper.monitor.entity.elastic.thread.check.realtime;

import lombok.Data;

import java.util.List;

@Data
public class ThreadCheckResult {
    private String overallStatus;
    private String readStatus;
    private String writeStatus;
    private String message;
    private List<ThreadCheckItem> checkItems;
    private List<ThreadPoolSuggestion> suggestions;
}