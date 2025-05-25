package com.dipper.monitor.entity.elastic.data;

import lombok.Data;

@Data
public class ProgressInfo {
    private int progress;
    private String status;
    private String format;
    private String filePath;
    private String error;

    public ProgressInfo() {
    }

    public ProgressInfo(String notFound, int i, String error) {
        this.status = notFound;
        this.progress = i;
        this.error = error;
    }
}
