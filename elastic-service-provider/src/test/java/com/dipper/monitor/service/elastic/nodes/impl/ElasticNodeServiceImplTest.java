package com.dipper.monitor.service.elastic.nodes.impl;

import com.dipper.monitor.BaseMonitorTest;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class ElasticNodeServiceImplTest extends BaseMonitorTest {

    @Autowired
    private ElasticNodeService elasticNodeService;

    @Test
    public void refreshNodes() {
        elasticNodeService.refreshNodes();
    }

    @Test
    public void nodePageList() {
    }
}