package com.dipper.monitor.task.thread;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.mapper.ElasticThreadMetricMapper;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.thread.ThreadMetricManagerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
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

    // 线程类型枚举（可根据实际情况扩展）
    private static final String[] THREAD_TYPES = {
            "bulk", "index", "search", "write", "management", "refresh", "merge"
    };

    @Autowired
    private ElasticClientService elasticClientService;

    @Autowired
    private ElasticThreadMetricMapper elasticThreadMetricMapper;
    
    @Autowired
    private ThreadMetricManagerService threadMetricManagerService;
    
    // 数据保留天数，默认30天
    @Value("${elastic.monitor.thread.retention-days:30}")
    private int retentionDays;

    // 模拟集群编码，实际可从配置文件读取
    @Value("${elastic.monitor.cluster-code:es-cluster-prod}")
    private String clusterCode;

    // 每隔 10 分钟执行一次
    @QuartzJob(cron = "0 0/10 * * * ?",
            author = "hydra",
            groupName = "hydra",
            jobDesc = "elastic线程池指标采集",
            editAble = true)
    public void collectAndSaveThreadPoolMetrics() {
        try {
            log.info("开始采集 ES 线程池监控指标...");

            String response = elasticClientService.executeGetApi(NODES_STATS_API);
            List<ThreadMetricEntity> metrics = parseThreadPoolResponse(response);

            if (!metrics.isEmpty()) {
                // 使用服务保存数据
                threadMetricManagerService.saveThreadMetrics(metrics);
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
            threadMetricManagerService.cleanHistoryData(retentionDays);
        } catch (Exception e) {
            log.error("清理历史线程池指标数据失败", e);
        }
    }

    /**
     * 解析 ES 返回的 thread_pool 数据
     */
    private List<ThreadMetricEntity> parseThreadPoolResponse(String response) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);

        List<ThreadMetricEntity> result = new ArrayList<>();

        // 遍历所有节点
        Iterator<String> fieldNames = rootNode.fieldNames();
        while (fieldNames.hasNext()) {
            String nodeId = fieldNames.next();

            JsonNode node = rootNode.get(nodeId);
            if (node == null || !node.has("thread_pool")) continue;

            JsonNode threadPoolNode = node.get("thread_pool");
            String nodeName = node.get("name").asText(); // 获取节点名称

            for (String threadType : THREAD_TYPES) {
                if (!threadPoolNode.has(threadType)) continue;

                JsonNode poolNode = threadPoolNode.get(threadType);

                ThreadMetricEntity metric = new ThreadMetricEntity();
                metric.setClusterCode(clusterCode);
                metric.setNodeName(nodeName);
                metric.setThreadType(threadType);
                metric.setActiveThreads(poolNode.get("active").asInt(0));
                metric.setQueueSize(poolNode.get("queue").asInt(0));
                metric.setRejectedCount(poolNode.get("rejected").asLong(0L));
                metric.setCompletedCount(poolNode.get("completed").asLong(0L));
                metric.setLargestSize(poolNode.get("largest").asInt(0));
                
                // 尝试获取线程池的CPU和内存使用情况
                // 注意：实际环境中可能需要通过其他API获取这些信息
                try {
                    if (poolNode.has("cpu_usage")) {
                        metric.setCpuUsage(Double.parseDouble(poolNode.get("cpu_usage").asText("0.00")));
                    } else {
                        metric.setCpuUsage(0.00d);
                    }
                    
                    if (poolNode.has("memory_usage")) {
                        metric.setMemoryUsage(poolNode.get("memory_usage").asLong(0L));
                    } else {
                        metric.setMemoryUsage(0L);
                    }
                } catch (Exception e) {
                    log.warn("解析CPU或内存使用率失败，使用默认值", e);
                    metric.setCpuUsage(0.00d);
                    metric.setMemoryUsage(0L);
                }
                
                metric.setCollectTime(LocalDateTime.now());

                result.add(metric);
            }
        }

        return result;
    }
}