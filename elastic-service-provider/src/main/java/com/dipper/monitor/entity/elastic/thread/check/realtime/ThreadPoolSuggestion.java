package com.dipper.monitor.entity.elastic.thread.check.realtime;

import lombok.Data;

import java.util.List;

@Data
public class ThreadPoolSuggestion {
    private String title;
    private String content;
    private List<String> actions;
}