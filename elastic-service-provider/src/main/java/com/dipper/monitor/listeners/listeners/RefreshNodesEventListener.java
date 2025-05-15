package com.dipper.monitor.listeners.listeners;

import com.dipper.monitor.entity.elastic.cluster.ClusterHealth;
import com.dipper.monitor.listeners.event.RefreshNodesEvent;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RefreshNodesEventListener implements ApplicationListener<RefreshNodesEvent> {

    @Autowired
    private ElasticRealNodeService elasticRealNodeService;
    @Autowired
    private ElasticClusterManagerService elasticClusterManagerService;



    @Override
    public void onApplicationEvent(RefreshNodesEvent event) {
        log.info("Received spring custom event - " + event.getMessage());
        // 获取集群的版本信息
        elasticClusterManagerService.updateClusterVersion();
        elasticRealNodeService.refreshNodes();
    }
}