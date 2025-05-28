package com.dipper.monitor.service.elastic.thread;

import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.entity.elastic.thread.check.pool.ThreadPoolTrendResult;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadCheckResult;
import com.dipper.monitor.entity.elastic.thread.hot.ThreadHotView;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadCharReq;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadChartSummary;

import java.io.IOException;
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
     * 执行线程环境检测
     * @return 线程检测结果
     */
    ThreadCheckResult threadRealTimeCheck() throws IOException;

    List<ThreadMetricEntity> getThreadMetrics(ThreadCharReq threadCharReq);

    /**
     * 获取线程的统计信息
     * @param threadCharReq
     * @return
     */
    List<ThreadChartSummary> threadChartSummary(ThreadCharReq threadCharReq);


}
