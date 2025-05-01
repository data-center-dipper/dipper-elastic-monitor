package com.dipper.monitor.utils.mock;

import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;

import java.util.ArrayList;
import java.util.List;

public class MockAllData {

    public static List<EsLifeCycleManagement> getLifeCycleError() {
        List<EsLifeCycleManagement> dataList = new ArrayList<>();

        dataList.add(new EsLifeCycleManagement("logs-2025-01", "Missing replica allocation for shard [0]."));
        dataList.add(new EsLifeCycleManagement("metrics-2025-02", "Index read-only; cannot allocate new shards."));
        dataList.add(new EsLifeCycleManagement("events-2025-03", "Cluster is in read-only mode."));
        dataList.add(new EsLifeCycleManagement("app-logs-2025-04", "Not enough nodes with required roles to hold the shard."));
        dataList.add(new EsLifeCycleManagement("error_logs-2025-05", "Shard failed to recover from snapshot."));
        dataList.add(new EsLifeCycleManagement("analytics-2025-06", "Disk watermark exceeded on node [node-1]."));

        return dataList;
    }

}
