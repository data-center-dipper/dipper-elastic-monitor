package com.dipper.monitor.entity.elastic.thread;

import lombok.Data;
import java.util.List;

@Data
public class ThreadCheckResult {
    private String overallStatus;  // 整体状态：正常、警告、异常
    private String readStatus;     // 读取状态：正常、压力较大、压力过大
    private String writeStatus;    // 写入状态：正常、压力较大、压力过大
    private List<ThreadCheckItem> checkItems; // 检测项列表
    private List<ThreadSuggestion> suggestions; // 优化建议
}