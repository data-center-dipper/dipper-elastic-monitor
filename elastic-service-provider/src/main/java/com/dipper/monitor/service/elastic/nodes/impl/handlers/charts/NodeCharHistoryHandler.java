package com.dipper.monitor.service.elastic.nodes.impl.handlers.charts;

import com.dipper.monitor.entity.db.elastic.ElasticNodeMetricEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.nodes.metric.NodeMetricHistoryReq;
import com.dipper.monitor.entity.elastic.nodes.metric.NodeMetricHistoryView;
import com.dipper.monitor.service.elastic.nodes.impl.NodeMetricStoreServiceImpl;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeCharHistoryHandler {

    private NodeMetricStoreServiceImpl nodeMetricStoreService;

    public NodeCharHistoryHandler(NodeMetricStoreServiceImpl nodeMetricStoreService) {
        this.nodeMetricStoreService = nodeMetricStoreService;
    }

    public NodeMetricHistoryView getNodeMetricHistory(NodeMetricHistoryReq nodeMetricHistoryReq) {

        // 1. 获取请求参数
        String nodeId = nodeMetricHistoryReq.getNodeId();
        String nodeName = nodeMetricHistoryReq.getNodeName();
        Instant startTime = nodeMetricHistoryReq.getStartTime();
        Instant endTime = nodeMetricHistoryReq.getEndTime();

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
                clusterCode, nodeId, nodeName, startTime, endTime);


        // 5. 提取所有指标数据
        NodeMetricHistoryView view = new NodeMetricHistoryView();

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

        List<Long> networkRxSizeList = new ArrayList<>();
        List<Long> networkRxPacketsList = new ArrayList<>();
        List<Long> networkTxSizeList = new ArrayList<>();
        List<Long> networkTxPacketsList = new ArrayList<>();

        List<Long> ioReadOperationsList = new ArrayList<>();
        List<Long> ioWriteOperationsList = new ArrayList<>();
        List<Long> ioReadSizeList = new ArrayList<>();
        List<Long> ioWriteSizeList = new ArrayList<>();

        List<Integer> shardsCountList = new ArrayList<>();
        List<Integer> indicesCountList = new ArrayList<>();

        for (ElasticNodeMetricEntity metric : historyMetrics) {
            // 时间戳
            timestamps.add(metric.getCollectTime().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());

            // CPU
            cpuUsage.add(metric.getCpuPercent());

            // 内存
            osMemTotalList.add(metric.getOsMemTotal());
            osMemFreeList.add(metric.getOsMemFree());
            osMemUsedList.add(metric.getOsMemUsed());
            osMemUsedPercentList.add(metric.getOsMemUsedPercent());
            osMemFreePercentList.add(metric.getOsMemFreePercent());
            jvmMemHeapUsedList.add(metric.getJvmMemHeapUsed());
            jvmMemHeapUsedPercentList.add(metric.getJvmMemHeapUsedPercent());
            jvmMemHeapMaxList.add(metric.getJvmMemHeapMax());

            // 磁盘
            diskTotalList.add(metric.getDiskTotal());
            diskUsedList.add(metric.getDiskUsed());
            diskAvailList.add(metric.getDiskAvail());
            diskPercentList.add(metric.getDiskPercent());

            // 文件描述符
            openFileDescriptorsList.add(metric.getOpenFileDescriptors());
            maxFileDescriptorsList.add(metric.getMaxFileDescriptors());

            // 线程
            threadsCountList.add(metric.getThreadsCount());

            // 网络
            networkRxSizeList.add(metric.getNetworkRxSize());
            networkRxPacketsList.add(metric.getNetworkRxPackets());
            networkTxSizeList.add(metric.getNetworkTxSize());
            networkTxPacketsList.add(metric.getNetworkTxPackets());

            // IO
            ioReadOperationsList.add(metric.getIoReadOperations());
            ioWriteOperationsList.add(metric.getIoWriteOperations());
            ioReadSizeList.add(metric.getIoReadSize());
            ioWriteSizeList.add(metric.getIoWriteSize());

            // 其他
            shardsCountList.add(metric.getShardsCount());
            indicesCountList.add(metric.getIndicesCount());
        }

        // 设置到 View 对象
        view.setTimestamps(timestamps);
        view.setCpuPercent(cpuUsage);

        view.setOsMemTotal(osMemTotalList);
        view.setOsMemFree(osMemFreeList);
        view.setOsMemUsed(osMemUsedList);
        view.setOsMemUsedPercent(osMemUsedPercentList);
        view.setOsMemFreePercent(osMemFreePercentList);

        view.setJvmMemHeapUsed(jvmMemHeapUsedList);
        view.setJvmMemHeapUsedPercent(jvmMemHeapUsedPercentList);
        view.setJvmMemHeapMax(jvmMemHeapMaxList);

        view.setDiskTotal(diskTotalList);
        view.setDiskUsed(diskUsedList);
        view.setDiskAvail(diskAvailList);
        view.setDiskPercent(diskPercentList);

        view.setOpenFileDescriptors(openFileDescriptorsList);
        view.setMaxFileDescriptors(maxFileDescriptorsList);

        view.setThreadsCount(threadsCountList);

        view.setNetworkRxSize(networkRxSizeList);
        view.setNetworkRxPackets(networkRxPacketsList);
        view.setNetworkTxSize(networkTxSizeList);
        view.setNetworkTxPackets(networkTxPacketsList);

        view.setIoReadOperations(ioReadOperationsList);
        view.setIoWriteOperations(ioWriteOperationsList);
        view.setIoReadSize(ioReadSizeList);
        view.setIoWriteSize(ioWriteSizeList);

        view.setShardsCount(shardsCountList);
        view.setIndicesCount(indicesCountList);

        // 返回单个节点的监控数据
        return view;
    }
}
