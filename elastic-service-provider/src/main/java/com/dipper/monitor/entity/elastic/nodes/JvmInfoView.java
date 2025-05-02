package com.dipper.monitor.entity.elastic.nodes;

import com.dipper.monitor.entity.elastic.original.nodes.info.nodes.JvmInfo;
import com.dipper.monitor.entity.elastic.original.nodes.stats.*;
import com.dipper.monitor.utils.UnitUtils;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Elasticsearch 节点 JVM 信息展示类
 */
@Data
public class JvmInfoView {

    // -------------------------- JVM 启动信息 --------------------------
    /**
     * JVM 启动时间（格式化为字符串）
     */
    private String startTime;

    /**
     * JVM 运行时长（小时:分钟:秒）
     */
    private String uptime;

    // -------------------------- 堆内存初始化配置 --------------------------
    /**
     * 堆内存初始大小（GB/MB 格式）
     */
    private String heapInitInGb;

    /**
     * 堆内存最大限制（GB/MB 格式）
     */
    private String heapMaxInGb;

    /**
     * 非堆内存最大限制（GB/MB 格式）
     */
    private String nonHeapMaxInGb;

    /**
     * 直接内存最大限制（GB/MB 格式）
     */
    private String directMaxInGb;

    // -------------------------- 当前内存使用情况 --------------------------

    /**
     * 当前堆内存已使用量（GB/MB 格式）
     */
    private String heapUsedInGb;

    /**
     * 当前堆内存使用百分比（0~100）
     */
    private Integer heapUsedPercent;

    /**
     * 当前非堆内存已使用量（GB/MB 格式）
     */
    private String nonHeapUsedInGb;

    // -------------------------- 垃圾回收器信息 --------------------------

    /**
     * 年轻代垃圾回收总次数
     */
    private Integer youngCollectionCount;

    /**
     * 年轻代垃圾回收累计耗时（毫秒）
     */
    private Long youngCollectionTimeInMs;

    /**
     * 老年代垃圾回收总次数
     */
    private Integer oldCollectionCount;

    /**
     * 老年代垃圾回收累计耗时（毫秒）
     */
    private Long oldCollectionTimeInMs;

    // -------------------------- 线程相关 --------------------------

    /**
     * 当前活跃线程数
     */
    private Integer threadCount;

    /**
     * 峰值线程数
     */
    private Integer peakThreadCount;

    // -------------------------- 构造方法 / 转换逻辑 --------------------------

    /**
     * 将原始的 JvmInfo 和 JVM 统计信息转换为视图对象
     *
     * @param jvmInfo  来自 nodes/info 接口的 JVM 配置信息
     * @param jvmStat  来自 nodes/stats 接口的 JVM 运行时统计信息
     */
    public void transToView(JvmInfo jvmInfo, JVM jvmStat) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 设置启动时间
        if (jvmInfo.getStartTimeInMillis() > 0) {
            this.startTime = sdf.format(new Date(jvmInfo.getStartTimeInMillis()));
        }

        // 设置内存初始化配置
        this.heapInitInGb = UnitUtils.transToGbOrMB(jvmInfo.getHeapInitInBytes());
        this.heapMaxInGb = UnitUtils.transToGbOrMB(jvmInfo.getHeapMaxInBytes());
        this.nonHeapMaxInGb = UnitUtils.transToGbOrMB(jvmInfo.getNonHeapMaxInBytes());
        this.directMaxInGb = UnitUtils.transToGbOrMB(jvmInfo.getDirectMaxInBytes());

        // 设置运行时内存状态
        if (jvmStat != null) {
            JvmMem mem = jvmStat.getMem();
            if (mem != null) {
                this.heapUsedInGb = UnitUtils.transToGbOrMB(mem.getHeap_used_in_bytes());
                this.heapUsedPercent = mem.getHeap_used_percent();
                this.nonHeapUsedInGb = UnitUtils.transToGbOrMB(mem.getNon_heap_used_in_bytes());
                this.nonHeapMaxInGb = UnitUtils.transToGbOrMB(mem.getNon_heap_committed_in_bytes());
            }

            // 设置线程信息
            Threads threads = jvmStat.getThreads();
            if (threads != null) {
                this.threadCount = threads.getCount();
                this.peakThreadCount = threads.getPeak_count();
            }

            // 设置 GC 信息
            GC gc = jvmStat.getGc();
            if (gc != null) {
                Map<String, GcCollector> collectors = gc.getCollectors();
                if (collectors != null) {
                    GcCollector young = collectors.get("young");
                    if (young != null) {
                        this.youngCollectionCount = young.getCollection_count();
                        this.youngCollectionTimeInMs = young.getCollection_time_in_millis();
                    }
                    GcCollector old = collectors.get("old");
                    if (old != null) {
                        this.oldCollectionCount = old.getCollection_count();
                        this.oldCollectionTimeInMs = old.getCollection_time_in_millis();
                    }
                }
            }
        }
    }

    /**
     * 格式化毫秒为 "X天 Y小时 Z分" 形式
     */
    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long days = seconds / (24 * 3600);
        long hours = (seconds % (24 * 3600)) / 3600;
        long minutes = (seconds % 3600) / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天 ");
        if (hours > 0) sb.append(hours).append("小时 ");
        if (minutes > 0) sb.append(minutes).append("分 ");

        return sb.length() > 0 ? sb.toString().trim() : "0分";
    }
}