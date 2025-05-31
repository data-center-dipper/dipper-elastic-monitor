package com.dipper.monitor.service.elastic.nodes.impl.handlers.charts;

import com.dipper.monitor.entity.db.elastic.ElasticNodeMetricEntity;
import com.dipper.monitor.entity.db.elastic.NodeStoreEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.nodes.metric.NodeMetricHistoryReq;
import com.dipper.monitor.entity.elastic.nodes.metric.NodeMetricHistoryView;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import com.dipper.monitor.service.elastic.nodes.impl.NodeMetricStoreServiceImpl;
import com.dipper.monitor.utils.UnitUtils;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NodeCharHistoryHandler {

    private NodeMetricStoreServiceImpl nodeMetricStoreService;
    private ElasticNodeStoreService elasticNodeStoreService;

    public NodeCharHistoryHandler(NodeMetricStoreServiceImpl nodeMetricStoreService, ElasticNodeStoreService elasticNodeStoreService) {
        this.nodeMetricStoreService = nodeMetricStoreService;
        this.elasticNodeStoreService = elasticNodeStoreService;
    }

    public NodeMetricHistoryView getNodeMetricHistory(NodeMetricHistoryReq nodeMetricHistoryReq) {

        // 1. 获取请求参数
        Integer nodeId = nodeMetricHistoryReq.getNodeId();
        Instant startTime = nodeMetricHistoryReq.getStartTime();
        Instant endTime = nodeMetricHistoryReq.getEndTime();
        List<String> metricTypes = nodeMetricHistoryReq.getMetricTypes();

        NodeStoreEntity entity = elasticNodeStoreService.getByNodeId(nodeId);
        String nodeName = entity.getHostName();

        // 2. 如果未指定时间范围，默认查询最近24小时
        if (endTime == null) {
            endTime = Instant.now();
        }
        if (startTime == null) {
            startTime = endTime.minus(24, ChronoUnit.HOURS);
        }

        // 3. 获取集群信息
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();

        // 4. 查询历史指标数据
        List<ElasticNodeMetricEntity> historyMetrics = nodeMetricStoreService.selectHistoryByCondition(
                clusterCode, nodeName, startTime, endTime);

        return getFullMetrics(historyMetrics, metricTypes);
    }

    private NodeMetricHistoryView getFullMetrics(List<ElasticNodeMetricEntity> historyMetrics, List<String> metricTypes) {
        NodeMetricHistoryView view = new NodeMetricHistoryView();

        // 初始化所有列表
        List<Long> timestamps = new ArrayList<>();
        List<Integer> cpuUsage = new ArrayList<>();

        List<Double> osMemTotalList = new ArrayList<>();
        List<Double> osMemFreeList = new ArrayList<>();
        List<Double> osMemUsedList = new ArrayList<>();
        List<Integer> osMemUsedPercentList = new ArrayList<>();
        List<Integer> osMemFreePercentList = new ArrayList<>();

        List<Double> jvmMemHeapUsedList = new ArrayList<>();
        List<Integer> jvmMemHeapUsedPercentList = new ArrayList<>();
        List<Double> jvmMemHeapMaxList = new ArrayList<>();

        List<String> diskTotalList = new ArrayList<>();
        List<String> diskUsedList = new ArrayList<>();
        List<String> diskAvailList = new ArrayList<>();
        List<Double> diskPercentList = new ArrayList<>();

        List<Integer> openFileDescriptorsList = new ArrayList<>();
        List<Integer> maxFileDescriptorsList = new ArrayList<>();

        List<Integer> threadsCountList = new ArrayList<>();

        List<Double> networkRxSizeList = new ArrayList<>();
        List<Long> networkRxPacketsList = new ArrayList<>();
        List<Double> networkTxSizeList = new ArrayList<>();
        List<Long> networkTxPacketsList = new ArrayList<>();

        List<Long> ioReadOperationsList = new ArrayList<>();
        List<Long> ioWriteOperationsList = new ArrayList<>();
        List<Double> ioReadSizeList = new ArrayList<>();
        List<Double> ioWriteSizeList = new ArrayList<>();

        List<Integer> shardsCountList = new ArrayList<>();
        List<Integer> indicesCountList = new ArrayList<>();

        // 遍历并按需添加数据
        // 遍历并按需添加数据
        for (ElasticNodeMetricEntity metric : historyMetrics) {
            timestamps.add(metric.getCollectTime().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());

            if (metricTypes.contains("cpuPercent")) {
                cpuUsage.add(metric.getCpuPercent());
            }

            // 内存相关字段转成 GB
            if (metricTypes.contains("osMemTotal"))
                osMemTotalList.add(UnitUtils.bytesToGB(metric.getOsMemTotal()));
            if (metricTypes.contains("osMemFree"))
                osMemFreeList.add(UnitUtils.bytesToGB(metric.getOsMemFree()));
            if (metricTypes.contains("osMemUsed"))
                osMemUsedList.add(UnitUtils.bytesToGB(metric.getOsMemUsed()));
            if (metricTypes.contains("osMemUsedPercent"))
                osMemUsedPercentList.add(metric.getOsMemUsedPercent());
            if (metricTypes.contains("osMemFreePercent"))
                osMemFreePercentList.add(metric.getOsMemFreePercent());

            // JVM 堆内存转成 GB
            if (metricTypes.contains("jvmMemHeapUsed"))
                jvmMemHeapUsedList.add(UnitUtils.bytesToGB(metric.getJvmMemHeapUsed()));
            if (metricTypes.contains("jvmMemHeapUsedPercent"))
                jvmMemHeapUsedPercentList.add(metric.getJvmMemHeapUsedPercent());
            if (metricTypes.contains("jvmMemHeapMax"))
                jvmMemHeapMaxList.add(UnitUtils.bytesToGB(metric.getJvmMemHeapMax()));

            // 磁盘容量转成 GB
            if (metricTypes.contains("diskTotal"))
                diskTotalList.add(String.valueOf(UnitUtils.bytesToGB(metric.getDiskTotal())));
            if (metricTypes.contains("diskUsed"))
                diskUsedList.add(String.valueOf(UnitUtils.bytesToGB(metric.getDiskUsed())));
            if (metricTypes.contains("diskAvail"))
                diskAvailList.add(String.valueOf(UnitUtils.bytesToGB(metric.getDiskAvail())));
            if (metricTypes.contains("diskPercent"))
                diskPercentList.add(metric.getDiskPercent());

            // 文件描述符等保持不变
            if (metricTypes.contains("openFileDescriptors"))
                openFileDescriptorsList.add(metric.getOpenFileDescriptors());
            if (metricTypes.contains("maxFileDescriptors"))
                maxFileDescriptorsList.add(metric.getMaxFileDescriptors());

            if (metricTypes.contains("threadsCount"))
                threadsCountList.add(metric.getThreadsCount());

            // 网络流量转成 MB
            if (metricTypes.contains("networkRxSize"))
                networkRxSizeList.add(UnitUtils.bytesToMB(metric.getNetworkRxSize()));
            if (metricTypes.contains("networkRxPackets"))
                networkRxPacketsList.add(metric.getNetworkRxPackets());
            if (metricTypes.contains("networkTxSize"))
                networkTxSizeList.add(UnitUtils.bytesToMB(metric.getNetworkTxSize()));
            if (metricTypes.contains("networkTxPackets"))
                networkTxPacketsList.add(metric.getNetworkTxPackets());

            // IO 操作大小转成 GB
            if (metricTypes.contains("ioReadOperations"))
                ioReadOperationsList.add(metric.getIoReadOperations());
            if (metricTypes.contains("ioWriteOperations"))
                ioWriteOperationsList.add(metric.getIoWriteOperations());
            if (metricTypes.contains("ioReadSize"))
                ioReadSizeList.add(UnitUtils.bytesToGB(metric.getIoReadSize()));
            if (metricTypes.contains("ioWriteSize"))
                ioWriteSizeList.add(UnitUtils.bytesToGB(metric.getIoWriteSize()));

            // 其他指标
            if (metricTypes.contains("shardsCount"))
                shardsCountList.add(metric.getShardsCount());
            if (metricTypes.contains("indicesCount"))
                indicesCountList.add(metric.getIndicesCount());
        }

        // 设置到 View 对象
        view.setTimestamps(timestamps);

        if (metricTypes.contains("cpuPercent")) view.setCpuPercent(cpuUsage);

        if (metricTypes.contains("osMemTotal")) view.setOsMemTotal(osMemTotalList);
        if (metricTypes.contains("osMemFree")) view.setOsMemFree(osMemFreeList);
        if (metricTypes.contains("osMemUsed")) view.setOsMemUsed(osMemUsedList);
        if (metricTypes.contains("osMemUsedPercent")) view.setOsMemUsedPercent(osMemUsedPercentList);
        if (metricTypes.contains("osMemFreePercent")) view.setOsMemFreePercent(osMemFreePercentList);

        if (metricTypes.contains("jvmMemHeapUsed")) view.setJvmMemHeapUsed(jvmMemHeapUsedList);
        if (metricTypes.contains("jvmMemHeapUsedPercent")) view.setJvmMemHeapUsedPercent(jvmMemHeapUsedPercentList);
        if (metricTypes.contains("jvmMemHeapMax")) view.setJvmMemHeapMax(jvmMemHeapMaxList);

        if (metricTypes.contains("diskTotal")) view.setDiskTotal(diskTotalList);
        if (metricTypes.contains("diskUsed")) view.setDiskUsed(diskUsedList);
        if (metricTypes.contains("diskAvail")) view.setDiskAvail(diskAvailList);
        if (metricTypes.contains("diskPercent")) view.setDiskPercent(diskPercentList);

        if (metricTypes.contains("openFileDescriptors")) view.setOpenFileDescriptors(openFileDescriptorsList);
        if (metricTypes.contains("maxFileDescriptors")) view.setMaxFileDescriptors(maxFileDescriptorsList);

        if (metricTypes.contains("threadsCount")) view.setThreadsCount(threadsCountList);

        if (metricTypes.contains("networkRxSize")) view.setNetworkRxSize(networkRxSizeList);
        if (metricTypes.contains("networkRxPackets")) view.setNetworkRxPackets(networkRxPacketsList);
        if (metricTypes.contains("networkTxSize")) view.setNetworkTxSize(networkTxSizeList);
        if (metricTypes.contains("networkTxPackets")) view.setNetworkTxPackets(networkTxPacketsList);

        if (metricTypes.contains("ioReadOperations")) view.setIoReadOperations(ioReadOperationsList);
        if (metricTypes.contains("ioWriteOperations")) view.setIoWriteOperations(ioWriteOperationsList);
        if (metricTypes.contains("ioReadSize")) view.setIoReadSize(ioReadSizeList);
        if (metricTypes.contains("ioWriteSize")) view.setIoWriteSize(ioWriteSizeList);

        if (metricTypes.contains("shardsCount")) view.setShardsCount(shardsCountList);
        if (metricTypes.contains("indicesCount")) view.setIndicesCount(indicesCountList);

        return view;
    }
}