package com.dipper.monitor.service.elastic.thread.impl;

import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadCharReq;
import com.dipper.monitor.entity.elastic.thread.chart.ThreadChartSummary;
import com.dipper.monitor.entity.elastic.thread.check.pool.ThreadPoolTrendResult;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadCheckResult;
import com.dipper.monitor.entity.elastic.thread.check.yanshi.ThreadPoolItem;
import com.dipper.monitor.entity.elastic.thread.hot.ThreadHotView;
import com.dipper.monitor.entity.elastic.thread.pengding.PendingTaskView;
import com.dipper.monitor.mapper.ElasticThreadMetricMapper;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.thread.ThreadManagerService;
import com.dipper.monitor.service.elastic.thread.ThreadPoolService;
import com.dipper.monitor.service.elastic.thread.handlers.HotThreadHandler;
import com.dipper.monitor.service.elastic.thread.handlers.ThreadChartSummaryHandler;
import com.dipper.monitor.service.elastic.thread.handlers.realcheck.ThreadHistoryCheckHandler;
import com.dipper.monitor.utils.Tuple2;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ThreadManagerServiceImpl implements ThreadManagerService {

    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticThreadMetricMapper elasticThreadMetricMapper;
    @Autowired
    private ThreadPoolService threadPoolService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    // 缓存线程列表，避免频繁请求ES
    private List<ThreadHotView> cachedThreadList = new ArrayList<>();

    @Override
    public List<ThreadHotView> threadPage() {
        // 如果缓存为空，则刷新线程列表
        if (cachedThreadList.isEmpty()) {
            cachedThreadList = refreshThreadList();
        }
        return cachedThreadList;
    }

    @Override
    public ThreadHotView getThreadDetail(Integer threadId) {
        // 从缓存中查找线程
        return cachedThreadList.stream()
                .filter(thread -> thread.getId().equals(threadId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ThreadHotView> refreshThreadList() {
        HotThreadHandler handler = new HotThreadHandler(elasticClientService);
        return handler.refreshThreadList();

    }



    @Override
    public void saveThreadMetrics(List<ThreadMetricEntity> metrics) {
        try {
            if (metrics == null || metrics.isEmpty()) {
                log.warn("没有线程池指标数据需要保存");
                return;
            }

            elasticThreadMetricMapper.batchInsert(metrics);
            log.info("成功保存 {} 条线程池指标数据", metrics.size());
        } catch (Exception e) {
            log.error("保存线程池指标数据失败", e);
            throw e;
        }
    }

    @Override
    public void cleanHistoryData(int retentionDays) {
        try {
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(retentionDays);
            int deletedCount = elasticThreadMetricMapper.deleteByCollectTimeBefore(beforeTime);
            log.info("成功清理 {} 条历史线程池指标数据（保留 {} 天）", deletedCount, retentionDays);
        } catch (Exception e) {
            log.error("清理历史线程池指标数据失败", e);
        }
    }





    @Override
    public ThreadCheckResult threadRealTimeCheck() throws IOException {
//        if(ApplicationUtils.isWindows()){
////            return MockAllData.threadRealTimeCheck();
//        }
        if (cachedThreadList.isEmpty()) {
            cachedThreadList = refreshThreadList();
        }
        List<ThreadPoolItem> threadPoolItems = threadPoolService.fetchThreadPool();
        ThreadHistoryCheckHandler threadRealTimeCheckHandler = new ThreadHistoryCheckHandler();
        return threadRealTimeCheckHandler.threadHistoryTimeCheck(threadPoolItems);
    }

    @Override
    public List<ThreadMetricEntity> getThreadMetrics(ThreadCharReq threadCharReq) {
        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        threadCharReq.setClusterCode(clusterCode);
        return elasticThreadMetricMapper.getThreadMetrics(threadCharReq);
    }

    @Override
    public List<ThreadChartSummary> threadChartSummary(ThreadCharReq threadCharReq) {
        List<ThreadMetricEntity> threadMetrics = getThreadMetrics(threadCharReq);
        ThreadChartSummaryHandler handler = new ThreadChartSummaryHandler(threadMetrics);
        return handler.threadChartSummary();
    }

    @Override
    public Tuple2<Integer, List<PendingTaskView>> pendingTasks(PageReq pageReq) throws IOException {
        com.dipper.monitor.service.elastic.thread.handlers.pending.PendingTaskHandler handler = new com.dipper.monitor.service.elastic.thread.handlers.pending.PendingTaskHandler(this,elasticClientService);
        return handler.pendingTasks(pageReq);

    }
}
