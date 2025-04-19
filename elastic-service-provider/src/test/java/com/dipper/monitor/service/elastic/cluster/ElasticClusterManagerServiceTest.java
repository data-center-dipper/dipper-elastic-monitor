package com.dipper.monitor.service.elastic.cluster;

import com.dipper.monitor.BaseMonitorTest;
import com.dipper.monitor.entity.elastic.cluster.ElasticClusterRegisterReq;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class ElasticClusterManagerServiceTest extends BaseMonitorTest {

    @Autowired
    private ElasticClusterManagerService elasticClusterManagerService;

    @Test
    public void registerCluster() {
        ElasticClusterRegisterReq clusterRegisterReq = new ElasticClusterRegisterReq();
        clusterRegisterReq.setClusterCode("aaa");
        clusterRegisterReq.setClusterName("test");
        clusterRegisterReq.setAddress("192.168.30.78:19201");
        clusterRegisterReq.setClusterDesc("http://192.168.30.78:19201");
        clusterRegisterReq.setCheckAddress(false);
        elasticClusterManagerService.registerCluster(clusterRegisterReq);
    }

    @Test
    public void updateCluster() {
        ElasticClusterRegisterReq clusterRegisterReq = new ElasticClusterRegisterReq();
        clusterRegisterReq.setClusterCode("aaa");
        clusterRegisterReq.setClusterName("test");
        clusterRegisterReq.setAddress("192.168.30.78:19201");
        clusterRegisterReq.setClusterDesc("http://192.168.30.78:19201");
        clusterRegisterReq.setCheckAddress(false);
        elasticClusterManagerService.updateCluster(clusterRegisterReq);
    }

    @Test
    public  void deleteCluster() {
    }

    @Test
    public  void setDefaultCluster() {
    }

    @Test
    public void setCurrentCluster() {
    }

    @Test
    public void getCurrentCluster() {
    }
}