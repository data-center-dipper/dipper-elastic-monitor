package com.dipper.monitor.service.elastic.slowsearch.impl;

import com.dipper.monitor.entity.elastic.slowsearch.kill.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillQueryReq;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryKillStoreService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;
import com.dipper.monitor.service.elastic.slowsearch.SlowSearchKillService;
import com.dipper.monitor.service.elastic.slowsearch.handlers.kill.KillQueryHandler;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.*;

@Service
@Slf4j
public class SlowSearchKillServiceImpl implements SlowSearchKillService {

    @Autowired
    private SlowQueryStoreService slowQueryStoreService;

    @Autowired
    private ElasticClientService elasticClientService;

    @Autowired
    private SlowQueryKillStoreService slowQueryKillStoreService;

    // 手动创建的线程池
    private ThreadPoolTaskExecutor killQueryExecutor;

    // 使用 @PostConstruct 初始化线程池
    @PostConstruct
    public void init() {
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        int maxPoolSize = corePoolSize * 2;
        long keepAliveTime = 60L;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(100);

        this.killQueryExecutor = new ThreadPoolTaskExecutor();
        this.killQueryExecutor.setCorePoolSize(corePoolSize);
        this.killQueryExecutor.setMaxPoolSize(maxPoolSize);
        this.killQueryExecutor.setKeepAliveSeconds((int) keepAliveTime);
        this.killQueryExecutor.setQueueCapacity(100);
        this.killQueryExecutor.setThreadNamePrefix("kill-query-pool-");
        this.killQueryExecutor.initialize();

        log.info("KillQuery 线程池已初始化: corePoolSize={}, maxPoolSize={}", corePoolSize, maxPoolSize);
    }

    @PreDestroy
    public void destroy() {
        if (killQueryExecutor != null) {
            killQueryExecutor.shutdown();
            log.info("KillQuery 线程池已关闭");
        }
    }

    @Override
    public boolean killQuery(KillQueryReq killQueryReq) {
        try {
            KillQueryHandler handler = new KillQueryHandler(
                    slowQueryStoreService,
                    elasticClientService,
                    slowQueryKillStoreService
            );
            handler.asyncKillQuery(killQueryReq, killQueryExecutor);
            return true;
        } catch (Exception e) {
            log.error("提交终止任务失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Tuple2<List<KillTimeoutRecord>, Long> killPage(KillPageReq killPageReq) {
        return ((SlowQueryKillStoreServiceImpl) slowQueryKillStoreService).queryKillRecordPage(killPageReq);
    }
    
    @Override
    public KillTimeoutRecord getKillRecordDetail(Integer recordId) {
        return ((SlowQueryKillStoreServiceImpl) slowQueryKillStoreService).getKillRecordDetail(recordId);
    }
}