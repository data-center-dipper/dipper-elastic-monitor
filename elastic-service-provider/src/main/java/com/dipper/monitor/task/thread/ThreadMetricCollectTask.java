package com.dipper.monitor.task.thread;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.mapper.ElasticThreadMetricMapper;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.thread.ThreadManagerService;
import com.dipper.monitor.service.elastic.thread.handlers.ThreadPoolParseHandler;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: chuanchuan.lcc
 * @date: 2025-05-22
 * @description: ES线程池指标采集定时任务
 */
@Component
@Slf4j
public class ThreadMetricCollectTask {

    // ES API 地址
    private static final String NODES_STATS_API = "/_nodes/stats/thread_pool";

    @Autowired
    private ElasticClientService elasticClientService;

    @Autowired
    private ThreadManagerService threadManagerService;

    // 数据保留天数，默认30天
    @Value("${elastic.monitor.thread.retention-days:30}")
    private int retentionDays;

    // 模拟集群编码，实际可从配置文件读取
    @Value("${elastic.monitor.cluster-code:es-cluster-prod}")
    private String clusterCode;

    // 每隔 10 分钟执行一次
    @QuartzJob(cron = "0 0/1 * * * ?",
            author = "hydra",
            groupName = "hydra",
            jobDesc = "elastic线程池指标采集",
            editAble = true)
    public void collectAndSaveThreadPoolMetrics() {
        try {
            log.info("开始采集 ES 线程池监控指标...");
            String response = elasticClientService.executeGetApi(NODES_STATS_API);

            CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
            String clusterCode = currentCluster.getClusterCode();

            ThreadPoolParseHandler threadPoolParseHandler = new ThreadPoolParseHandler();
            List<ThreadMetricEntity> metrics = threadPoolParseHandler.parseThreadPoolResponse(response, clusterCode);


            if (!metrics.isEmpty()) {
                // 使用服务保存数据
                threadManagerService.saveThreadMetrics(metrics);
                log.info("成功采集并保存 {} 条线程池指标数据", metrics.size());
            } else {
                log.warn("未解析到有效的线程池指标数据");
            }

        } catch (Exception e) {
            log.error("采集 ES 线程池指标失败", e);
        }
    }

    // 每天凌晨2点执行清理历史数据
    @QuartzJob(cron = "0 0 2 * * ?",
            author = "hydra",
            groupName = "hydra",
            jobDesc = "清理历史线程池指标数据",
            editAble = true)
    public void cleanHistoryThreadMetrics() {
        try {
            log.info("开始清理历史线程池指标数据，保留 {} 天...", retentionDays);
            threadManagerService.cleanHistoryData(retentionDays);
        } catch (Exception e) {
            log.error("清理历史线程池指标数据失败", e);
        }
    }


}