package com.dipper.monitor.task.slow_search;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.config.SlowSearchConfig;
import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.slowsearch.task.SlowQueryTaskEntity;
import com.dipper.monitor.service.elastic.slowsearch.RealSlowSearchService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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




    @Autowired
    private SlowQueryStoreService slowQueryStoreService;
    @Autowired
    private RealSlowSearchService realSlowSearchService;
    @Autowired
    private SlowSearchConfig slowSearchConfig;


    // 每10分钟执行一次
    @QuartzJob(cron = "0 0/1 * * * ?",
            author = "hydra",
            groupName = "elastic_monitor",
            jobDesc = "采集Elasticsearch中的慢查询信息",
            editAble = true)
    public void collectSlowQueries() {
        try {
            log.info("开始采集ES慢查询信息...");
            List<SlowQueryTaskEntity> taskSlowQueries = realSlowSearchService.getRelaSlowQuery();
            // 转成 List<SlowQueryEntity> slowQueries
            List<SlowQueryEntity> slowQueries = realSlowSearchService.transToSlowQueryEntity(taskSlowQueries);
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
            int retentionDays = slowSearchConfig.getRetentionDays();
            log.info("开始清理历史慢查询数据，保留 {} 天...", retentionDays);
            slowQueryStoreService.cleanHistoryData(retentionDays);
        } catch (Exception e) {
            log.error("清理历史慢查询数据失败", e);
        }
    }
}
