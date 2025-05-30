package com.dipper.monitor.task.node;

import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class NodeListRefreshTask {

    @Autowired
    private ElasticRealNodeService elasticRealNodeService;

    /**
     * 每5分钟执行一次节点指标收集任务
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void collectNodeMetrics() {
        log.info("开始刷新节点列表");
        try {
            // 获取当前集群信息
            CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
            if (currentCluster == null) {
                log.warn("未找到当前集群信息，无法执行节点指标收集");
                return;
            }
            elasticRealNodeService.refreshNodes();
        } catch (Exception e) {
            log.error("节点指标收集任务执行失败", e);
        }

    }
}
