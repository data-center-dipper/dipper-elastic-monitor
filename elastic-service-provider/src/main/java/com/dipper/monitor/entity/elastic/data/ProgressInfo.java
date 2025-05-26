package com.dipper.monitor.entity.elastic.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressInfo {
    // 状态：pending, running, completed, failed
    private String status;
    
    // 进度百分比：0-100
    private int progress;
    
    // 进度消息
    private String message;
    
    // 错误计数
    private int errorCount;

    private String error;

    private String filePath;
    
    public ProgressInfo(String status, int progress, String message) {
        this.status = status;
        this.progress = progress;
        this.message = message;
        this.errorCount = 0;
    }

    public ProgressInfo(String status, int progress, String message, int errorCount) {
        this.status = status;
        this.progress = progress;
        this.message = message;
        this.errorCount = errorCount;
    }
}
