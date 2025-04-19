package com.dipper.monitor.task.nodes;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ElasticNodeMetricTask {

    @Autowired
    private ElasticNodeStoreService elasticNodeStoreService;


    @QuartzJob(cron = "0 0/10 * * * ?",
//    @QuartzJob(cron = "0/10 * * * * ?",
            author = "hydra",
            groupName = "hydra",
            jobDesc = "elastic节点信息监控",
            editAble = true)

    public void elasticNodesUpdateTask() throws Exception {
    }

}

