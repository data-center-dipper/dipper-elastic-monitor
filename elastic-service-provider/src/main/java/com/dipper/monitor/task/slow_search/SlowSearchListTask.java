package com.dipper.monitor.task.slow_search;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;
import com.dipper.monitor.service.elastic.slowsearch.SlowSearchService;
import com.dipper.monitor.service.elastic.slowsearch.handlers.SlowQueryParseHandler;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: hydra
 * @date: 2023-05-22
 * @description: ES慢查询监控定时任务
 */
@Component
@Slf4j
public class SlowSearchListTask {

    // ES 慢查询API地址
    private static final String SLOW_SEARCH_API = "/_nodes/stats/indices/search"; // 可能需要根据实际ES API调整

    @Autowired
    private ElasticClientService elasticClientService;

    @Autowired
    private SlowQueryStoreService slowQueryStoreService;

    // 数据保留天数，默认7天
    @Value("${elastic.monitor.slow-query.retention-days:7}")
    private int retentionDays;

    // 慢查询阈值，默认1000ms
    @Value("${elastic.monitor.slow-query.threshold:1000}")
    private int slowQueryThreshold;

    // 每10分钟执行一次
    @QuartzJob(cron = "0 0/10 * * * ?",
            author = "hydra",
            groupName = "elastic_monitor",
            jobDesc = "采集Elasticsearch中的慢查询信息",
            editAble = true)
    public void collectSlowQueries() {
        try {
            log.info("开始采集ES慢查询信息...");
            String response = elasticClientService.executeGetApi(SLOW_SEARCH_API);

            CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
            String clusterCode = currentCluster.getClusterCode();

            com.dipper.monitor.service.elastic.slowsearch.handlers.SlowQueryParseHandler parseHandler = new SlowQueryParseHandler();
            List<SlowQueryEntity> slowQueries = parseHandler.parseSlowQueryResponse(response, clusterCode, slowQueryThreshold);

            if (!slowQueries.isEmpty()) {
                // 保存慢查询数据
                slowQueryStoreService.saveSlowQueries(slowQueries);
                log.info("成功采集并保存 {} 条慢查询数据", slowQueries.size());
            } else {
                log.info("本次未发现慢查询数据");
            }

        } catch (Exception e) {
            log.error("采集ES慢查询信息失败", e);
        }
    }

    // 每天凌晨3点执行清理历史数据
    @QuartzJob(cron = "0 0 3 * * ?",
            author = "hydra",
            groupName = "elastic_monitor",
            jobDesc = "清理历史慢查询数据",
            editAble = true)
    public void cleanHistorySlowQueries() {
        try {
            log.info("开始清理历史慢查询数据，保留 {} 天...", retentionDays);
            slowQueryStoreService.cleanHistoryData(retentionDays);
        } catch (Exception e) {
            log.error("清理历史慢查询数据失败", e);
        }
    }
}
