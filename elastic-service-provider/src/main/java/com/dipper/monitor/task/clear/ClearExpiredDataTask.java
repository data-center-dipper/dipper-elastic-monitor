package com.dipper.monitor.task.clear;

import com.dipper.monitor.config.SlowSearchConfig;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;
import com.dipper.monitor.service.elastic.thread.ThreadManagerService;
import com.dipper.monitor.task.AbstractITask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClearExpiredDataTask    extends AbstractITask {

    @Autowired
    private ThreadManagerService threadManagerService;
    @Autowired
    private SlowSearchConfig slowSearchConfig;
    @Autowired
    private SlowQueryStoreService slowQueryStoreService;

    // 数据保留天数，默认30天
    @Value("${elastic.monitor.thread.retention-days:30}")
    private int retentionDays;

    @Override
    public String getCron() {
        return "0 0 2 * * ?";
    }

    @Override
    public void setCron(String cron) {

    }

    @Override
    public String getAuthor() {
        return "lcc";
    }

    @Override
    public String getJobDesc() {
        return "清理指标等过期数据";
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void execute() {
        try {
            log.info("开始清理历史线程池指标数据，保留 {} 天...", retentionDays);
            threadManagerService.cleanHistoryData(retentionDays);
        } catch (Exception e) {
            log.error("清理历史线程池指标数据失败", e);
        }

        try {
            int retentionDays = slowSearchConfig.getRetentionDays();
            log.info("开始清理历史慢查询数据，保留 {} 天...", retentionDays);
            slowQueryStoreService.cleanHistoryData(retentionDays);
        } catch (Exception e) {
            log.error("清理历史慢查询数据失败", e);
        }
    }

    @Override
    public String getTaskName() {
        return "清理过期数据";
    }
}
