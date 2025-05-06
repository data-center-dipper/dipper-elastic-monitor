package com.dipper.monitor.utils;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class DiscardOldestThreadPolicy implements RejectedExecutionHandler {
    private Long discardOldCount = Long.valueOf(0L);
    private Long startTime = Long.valueOf(System.currentTimeMillis());

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            Long long_ = this.discardOldCount;
            this.discardOldCount = Long.valueOf(this.discardOldCount.longValue() + 1L);
            checkDiscardPersent();
            e.getQueue().poll();
            e.execute(r);
        }
    }

    private void checkDiscardPersent() {
        Long nowTime = Long.valueOf(System.currentTimeMillis());
    }

    public Long getDiscardOldCount() {
        return this.discardOldCount;
    }

    public Long getInterval() {
        return Long.valueOf(System.currentTimeMillis() - this.startTime.longValue());
    }

    public Double getPersent() {
        if (getInterval().longValue() == 0L) {
            return Double.valueOf(0.0D);
        }
        Double persent = Double.valueOf(this.discardOldCount.doubleValue() / (getInterval().longValue() / 1000L / 60L));
        return persent;
    }
}