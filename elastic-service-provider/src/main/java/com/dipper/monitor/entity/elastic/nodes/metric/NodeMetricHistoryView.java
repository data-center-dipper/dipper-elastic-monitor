package com.dipper.monitor.entity.elastic.nodes.metric;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import java.util.List;

/**
 * 节点历史监控视图类，包含所有重要指标
 */
@Data
public class NodeMetricHistoryView {
    // 时间戳（毫秒）
    private List<Long> timestamps;

    // CPU 相关
    private List<Integer> cpuPercent;

    // 内存相关
    private List<Double> osMemTotal;
    private List<Double> osMemFree;
    private List<Double> osMemUsed;
    private List<Integer> osMemUsedPercent;
    private List<Integer> osMemFreePercent;
    private List<Double> jvmMemHeapUsed;
    private List<Integer> jvmMemHeapUsedPercent;
    private List<Double> jvmMemHeapMax;

    // 磁盘相关
    private List<String> diskTotal;
    private List<String> diskUsed;
    private List<String> diskAvail;
    private List<Double> diskPercent;

    // 文件描述符
    private List<Integer> openFileDescriptors;
    private List<Integer> maxFileDescriptors;

    // 线程相关
    private List<Integer> threadsCount;

    // 网络相关
    private List<Long> networkRxSize;
    private List<Long> networkRxPackets;
    private List<Long> networkTxSize;
    private List<Long> networkTxPackets;

    // IO相关
    private List<Long> ioReadOperations;
    private List<Long> ioWriteOperations;
    private List<Long> ioReadSize;
    private List<Long> ioWriteSize;

    // 其他统计
    private List<Integer> shardsCount;
    private List<Integer> indicesCount;
}