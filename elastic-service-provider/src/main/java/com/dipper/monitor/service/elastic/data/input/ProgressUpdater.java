package com.dipper.monitor.service.elastic.data.input;



@FunctionalInterface
public interface ProgressUpdater {
    void updateProgress(int progress, String message);
}
