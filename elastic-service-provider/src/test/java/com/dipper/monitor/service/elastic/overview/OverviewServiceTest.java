package com.dipper.monitor.service.elastic.overview;

import com.dipper.monitor.BaseMonitorTest;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.cluster.ClusterStatusView;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class OverviewServiceTest extends BaseMonitorTest {

    @Autowired
    private OverviewService overviewService;

    /**
     * epoch = "1745038208"
     * timestamp = "04:50:08"
     * cluster = "es_1.server1"
     * status = "green"
     * nodeTotal = 4
     * nodeData = 4
     * shards = "746"
     * pri = "415"
     * relo = "0"
     * init = "0"
     * unassign = "0"
     * pendingTasks = "0"
     * maxTaskWaitTime = "-"
     * activeShardsPercent = "100.0%"
     * clusterStatus = {ClusterStatsParse@11010} "ClusterStatsParse(timestamp=1745038211483, indices=ClusterIndices(count=126, shards=Shards(total=746, primaries=415, replication=0.7975903614457831), docs=Docs(count=92715770, deleted=37359609), store=Store(size_in_bytes=39121746133, total_data_set_size_in_bytes=39121746133, reserved_in_bytes=0)))"
     *  timestamp = 1745038211483
     *  indices = {ClusterIndices@14112} "ClusterIndices(count=126, shards=Shards(total=746, primaries=415, replication=0.7975903614457831), docs=Docs(count=92715770, deleted=37359609), store=Store(size_in_bytes=39121746133, total_data_set_size_in_bytes=39121746133, reserved_in_bytes=0))"
     *   count = 126
     *   shards = {Shards@14114} "Shards(total=746, primaries=415, replication=0.7975903614457831)"
     *   docs = {Docs@14115} "Docs(count=92715770, deleted=37359609)"
     *   store = {Store@14116} "Store(size_in_bytes=39121746133, total_data_set_size_in_bytes=39121746133, reserved_in_bytes=0)"
     */
    @Test
    public void getClusterStatus() {
        ClusterStatusView clusterStatus = overviewService.getClusterStatus();
    }

    @Test
    public void clusterError() {
        PageReq pageReq = new PageReq();
        pageReq.setPageNum(1);
        pageReq.setPageSize(10);
         overviewService.getLifeCycleError(pageReq);
    }
}