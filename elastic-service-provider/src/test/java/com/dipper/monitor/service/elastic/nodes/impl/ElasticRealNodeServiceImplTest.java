package com.dipper.monitor.service.elastic.nodes.impl;

import com.dipper.monitor.BaseMonitorTest;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ElasticRealNodeServiceImplTest extends BaseMonitorTest {

    @Autowired
    private ElasticRealNodeService elasticRealNodeService;

    @Test
    public void refreshNodes() {
        elasticRealNodeService.refreshNodes();
    }

    @Test
    public void nodePageList() {
    }
}