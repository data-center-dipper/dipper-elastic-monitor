package com.dipper.monitor.service.elastic.thread.handlers.pool;

import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.entity.elastic.thread.check.pool.ThreadPoolTrendResult;
import com.dipper.monitor.entity.elastic.thread.check.yanshi.ThreadPoolItem;

import java.util.List;
import java.util.function.Function;

public class ThreadPoolAnalyzer {

    /**
     * 判断某字段是否呈递增趋势（连续增长）
     */
    public static <T> boolean isTrendIncreasing(List<T> list, Function<T, Integer> getter) {
        if (list.size() < 3) return false;

        int count = 0;
        for (int i = 1; i < list.size(); i++) {
            int prev = getter.apply(list.get(i - 1));
            int curr = getter.apply(list.get(i));
            if (curr > prev) count++;
        }

        return count >= 3;
    }

    /**
     * 判断某个线程池项是否有队列或活跃线程持续增长
     */
    public static ThreadPoolTrendResult analyzeTrend(String nodeName, String poolName, List<ThreadPoolItem> items) {
        if(ApplicationUtils.isLinux()){
            boolean hasNonZero = items.stream().anyMatch(i -> i.getQueue() > 0 || i.getActive() > 0);
            if (!hasNonZero) return null;
        }

        boolean queueIncreasing = isTrendIncreasing(items, ThreadPoolItem::getQueue);
        boolean activeIncreasing = isTrendIncreasing(items, ThreadPoolItem::getActive);

        if(ApplicationUtils.isLinux()){
            if (!queueIncreasing && !activeIncreasing) return null;
        }


        int lastActive = items.get(items.size() - 1).getActive();
        int lastQueue = items.get(items.size() - 1).getQueue();

        return new ThreadPoolTrendResult(nodeName, poolName, lastActive, lastQueue, queueIncreasing, activeIncreasing);
    }
}