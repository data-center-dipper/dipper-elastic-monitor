package com.dipper.monitor.utils;

import jakarta.validation.constraints.NotNull;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class CommonThreadFactory implements ThreadFactory {
    private static AtomicLong threadIndex = new AtomicLong();
    private static String threadPrefix = "default";

    public CommonThreadFactory(String threadPrefix) {
        CommonThreadFactory.threadPrefix = threadPrefix;
    }

    public Thread newThread(@NotNull Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(threadPrefix + "-thread-" + threadIndex.incrementAndGet());
        return thread;
    }
}