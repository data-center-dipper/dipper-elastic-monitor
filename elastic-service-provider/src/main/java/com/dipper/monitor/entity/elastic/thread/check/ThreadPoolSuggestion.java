package com.dipper.monitor.entity.elastic.thread.check;

import lombok.Data;
import java.util.List;

@Data
public class ThreadPoolSuggestion {
    private String title;       // 建议标题
    private String content;     // 建议内容
    private List<String> actions; // 建议操作
}