package com.dipper.monitor.entity.elastic.thread.check;


import lombok.Data;

import java.util.List;

@Data
public class ThreadPoolCheckResult {
    private String status; // 整体状态：正常 / 警告 / 异常
    private List<ThreadPoolStat> poolStats;
    private List<ThreadPoolCheckItem> checkItems;
    private List<ThreadPoolSuggestion> suggestions;
}