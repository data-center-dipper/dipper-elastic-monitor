package com.dipper.monitor.entity.elastic.thread.check.realtime;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ThreadPoolSuggestion {
    private String title;
    private String content;
    private List<String> actions;
}