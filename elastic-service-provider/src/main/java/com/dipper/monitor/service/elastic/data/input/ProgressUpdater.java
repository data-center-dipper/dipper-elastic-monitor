package com.dipper.monitor.service.elastic.data.input;

@FunctionalInterface
public interface ProgressUpdater {
    /**
     * 更新进度信息
     * @param progress 进度百分比
     * @param message 进度消息
     * @param errorCount 错误计数
     */
    void updateProgress(int progress, String message, int errorCount);
}
