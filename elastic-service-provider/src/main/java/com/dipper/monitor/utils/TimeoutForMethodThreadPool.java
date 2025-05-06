package com.dipper.monitor.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class TimeoutForMethodThreadPool {
    private static final Logger log = LoggerFactory.getLogger(TimeoutForMethodThreadPool.class);

    private ScheduledExecutorService scheduledExecutorService;

    private int defaultCorePoolSize = 10;
    private int defaultMaximumPoolSize = 100;
    private int defaultKeepAliveTime = 10;
    private int defaultCapacity = 100;

    private DiscardOldestThreadPolicy discardOldestThreadPolicy;

    private int maxAliveThreadCount = 0;
    private int maxBlockingQueueCount = 0;
    private int maxCorePoolSize = 0;

    private volatile ThreadPoolExecutor curentExecutor;

    @PostConstruct
    public void init() {
        this.curentExecutor = initNewPool();
    }

    private void checkAddcurrent() {
        boolean shutdown = this.curentExecutor.isShutdown();
        boolean terminated = this.curentExecutor.isTerminated();
        boolean terminating = this.curentExecutor.isTerminating();

        if (shutdown || terminated || terminating) {
            log.info("线程池被关闭需要重新初始化一个");
            this.curentExecutor = initNewPool();
        }

        checkDiscard();

        int activeCount = this.curentExecutor.getActiveCount();
        int size = this.curentExecutor.getQueue().size();
        long taskCount = this.curentExecutor.getTaskCount();
        long completedTaskCount = this.curentExecutor.getCompletedTaskCount();
        int corePoolSize = this.curentExecutor.getCorePoolSize();
        int largestPoolSize = this.curentExecutor.getLargestPoolSize();
        int maximumPoolSize = this.curentExecutor.getMaximumPoolSize();
        long keepAliveTime = this.curentExecutor.getKeepAliveTime(TimeUnit.MINUTES);

        log.info("activeCount:{},size:{} taskCount:{} completedTaskCount:{} corePoolSize:{} largestPoolSize:{} maximumPoolSize:{} keepAliveTime:{}",
                activeCount, size, taskCount, completedTaskCount, corePoolSize, largestPoolSize, maximumPoolSize, keepAliveTime);

        if (this.maxAliveThreadCount < activeCount) {
            this.maxAliveThreadCount = activeCount;
        }
        if (this.maxBlockingQueueCount < size) {
            this.maxBlockingQueueCount = size;
        }
        if (this.maxCorePoolSize < corePoolSize) {
            this.maxCorePoolSize = corePoolSize;
        }
        log.info("当前最大值：maxAliveThreadCount：{} maxBlockingQueueCount:{} maxCorePoolSize:{}",
                this.maxAliveThreadCount, this.maxBlockingQueueCount, this.maxCorePoolSize);
    }

    private void checkDiscard() {
        int activeCount = this.curentExecutor.getActiveCount();
        int size = this.curentExecutor.getQueue().size();
        int corePoolSize = this.curentExecutor.getCorePoolSize();
        if (corePoolSize == this.defaultCorePoolSize && size == this.defaultCapacity) {
            Long discardOldCount = this.discardOldestThreadPolicy.getDiscardOldCount();
            Long interval = this.discardOldestThreadPolicy.getInterval();
            Double persent = this.discardOldestThreadPolicy.getPersent();
            log.info("当前丢弃的总数：{} 时间范围：{} 概率：{}", discardOldCount, interval, persent);
        }
    }

    private ThreadPoolExecutor initNewPool() {
        this.discardOldestThreadPolicy = new DiscardOldestThreadPolicy();
        return new ThreadPoolExecutor(this.defaultCorePoolSize, this.defaultMaximumPoolSize, this.defaultKeepAliveTime, TimeUnit.MINUTES, new ArrayBlockingQueue<>(this.defaultCapacity), new CommonThreadFactory("method-for-timeout"), this.discardOldestThreadPolicy);
    }

    public <T> Future<T> submit(Supplier<T> supplier) {
        CompletableFuture<T> feature = CompletableFuture.supplyAsync(supplier, this.curentExecutor);
        checkAddcurrent();
        return feature;
    }
}