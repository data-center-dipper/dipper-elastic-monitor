package com.dipper.monitor.service.elastic.slowsearch.handlers.kill;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillQueryReq;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillQueryResult;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryKillStoreService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
public class KillQueryHandler {

    private final SlowQueryStoreService slowQueryStoreService;
    private final ElasticClientService elasticClientService;
    private final SlowQueryKillStoreService slowQueryKillStoreService;

    // 超时时间
    private static final long KILL_TIMEOUT = 10; // 秒

    public KillQueryHandler(SlowQueryStoreService slowQueryStoreService,
                            ElasticClientService elasticClientService,
                            SlowQueryKillStoreService slowQueryKillStoreService) {
        this.slowQueryStoreService = slowQueryStoreService;
        this.elasticClientService = elasticClientService;
        this.slowQueryKillStoreService = slowQueryKillStoreService;
    }

    public void asyncKillQuery(KillQueryReq killQueryReq, ThreadPoolTaskExecutor killQueryExecutor) {
        Future<?> future = CompletableFuture.runAsync(() -> {
            KillQueryResult result = new KillQueryResult();
            result.setQueryId(killQueryReq.getQueryId());

            try {
                // 获取任务详情
                SlowQueryEntity entity = slowQueryStoreService.getQueryDetail(killQueryReq.getQueryId());
                if (entity == null || !"running".equals(entity.getStatus())) {
                    result.setSuccess(false);
                    result.setReason("任务不存在或不在运行中");
                    recordKillResult(result);
                    return;
                }

                String taskId = entity.getTaskId();
                if (StringUtils.isBlank(taskId)) {
                    result.setSuccess(false);
                    result.setReason("任务ID为空");
                    recordKillResult(result);
                    return;
                }

                result.setTaskId(taskId);

                // 提交取消请求
                String cancelTaskUrl = "/tasks/" + taskId + "/_cancel";
                String responseStr = elasticClientService.executeGetApi(cancelTaskUrl);
                JSONObject response = JSON.parseObject(responseStr);

                boolean acknowledged = response != null && response.getBooleanValue("acknowledged");

                if (acknowledged) {
                    // 成功终止
                    entity.setStatus("killed");
                    slowQueryStoreService.updateSlowQuery(entity);

                    result.setSuccess(true);
                    result.setReason("成功终止");
                } else {
                    // 失败
                    result.setSuccess(false);
                    result.setReason("ES响应未确认");
                }

            } catch (Exception e) {
                result.setSuccess(false);
                result.setReason("异常：" + e.getMessage());
                log.error("终止查询异常", e);
            } finally {
                recordKillResult(result);
            }
        }, killQueryExecutor); // 使用注入的线程池
    }

    private void recordKillResult(KillQueryResult result) {
        slowQueryKillStoreService.saveKillRecord(result);
    }
}