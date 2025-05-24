package com.dipper.monitor.service.elastic.thread;

import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.entity.elastic.thread.ThreadCheckResult;
import com.dipper.monitor.entity.elastic.thread.ThreadHotView;
import com.dipper.monitor.entity.elastic.thread.ThreadPageReq;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadCharReq;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadChartSummary;
import com.dipper.monitor.utils.Tuple2;

import java.time.LocalDateTime;
import java.util.List;

public interface ThreadManagerService {
    /**
     * 分页查询热点线程
     * @return 线程列表和总数
     */
    List<ThreadHotView> threadPage();

    /**
     * 获取线程详情
     * @param threadId 线程ID
     * @return 线程详情
     */
    ThreadHotView getThreadDetail(Integer threadId);

    /**
     * 刷新线程列表
     * @return 最新线程列表
     */
    List<ThreadHotView> refreshThreadList();

    /**
     * 保存线程池指标数据
     * @param metrics 线程池指标列表
     */
    void saveThreadMetrics(List<ThreadMetricEntity> metrics);

    /**
     * 清理历史数据
     * @param retentionDays 保留天数
     */
    void cleanHistoryData(int retentionDays);

    /**
     * 根据集群、节点和线程类型查询指标
     * @param clusterCode 集群编码
     * @param nodeName 节点名称
     * @param threadType 线程类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 线程池指标列表
     */
    List<ThreadMetricEntity> getThreadMetricsByClusterAndNode(String clusterCode, String nodeName,
                                                              String threadType, LocalDateTime startTime,
                                                              LocalDateTime endTime);

    /**
     * 根据集群和线程类型查询指标
     * @param clusterCode 集群编码
     * @param threadType 线程类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 线程池指标列表
     */
    List<ThreadMetricEntity> getThreadMetricsByClusterAndType(String clusterCode, String threadType,
                                                              LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取最新的线程池指标
     * @param clusterCode 集群编码
     * @param nodeName 节点名称
     * @param threadType 线程类型
     * @return 最新的线程池指标
     */
    ThreadMetricEntity getLatestThreadMetric(String clusterCode, String nodeName, String threadType);

    /**
     * 执行线程环境检测
     * @return 线程检测结果
     */
    ThreadCheckResult checkThreadEnvironment();

    List<ThreadMetricEntity> getThreadMetrics(ThreadCharReq threadCharReq);

    /**
     * 获取线程的统计信息
     * @param threadCharReq
     * @return
     */
    List<ThreadChartSummary> threadChartSummary(ThreadCharReq threadCharReq);
}
