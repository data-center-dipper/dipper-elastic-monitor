package com.dipper.monitor.task.slow_search;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.config.SlowSearchConfig;
import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.slowsearch.task.SlowQueryTaskEntity;
import com.dipper.monitor.service.elastic.slowsearch.RealSlowSearchService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;
import com.dipper.monitor.task.AbstractITask;
import com.dipper.monitor.task.ITask;
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
public class SlowSearchListTask   extends AbstractITask  {



    @Autowired
    private SlowQueryStoreService slowQueryStoreService;
    @Autowired
    private RealSlowSearchService realSlowSearchService;
    @Autowired
    private SlowSearchConfig slowSearchConfig;


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


    @Override
    public String getCron() {
        return "0 0/1 * * * ?";
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
        return "采集Elasticsearch中的慢查询信息";
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void execute() {
        collectSlowQueries();
    }

    @Override
    public String getTaskName() {
        return "collectSlowQueries";
    }
}
