package com.dipper.monitor.utils.mock;

import com.alibaba.fastjson.JSONObject;
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

    public static List<JSONObject> getShardError() {
        List<JSONObject> shardList = new ArrayList<>();

        // 模拟 UNASSIGNED 分片
        JSONObject shard1 = new JSONObject();
        shard1.put("index", "logs-2025-01");
        shard1.put("shard", "0");
        shard1.put("prirep", "p");
        shard1.put("state", "UNASSIGNED");
        shard1.put("docs", "0");
        shard1.put("store", "?");
        shard1.put("ip", "");
        shard1.put("node", "");

        // 模拟 RELOCATING 分片
        JSONObject shard2 = new JSONObject();
        shard2.put("index", "metrics-2025-02");
        shard2.put("shard", "1");
        shard2.put("prirep", "r");
        shard2.put("state", "RELOCATING");
        shard2.put("docs", "10000");
        shard2.put("store", "23.4mb");
        shard2.put("ip", "192.168.1.102");
        shard2.put("node", "node-2");

        // 模拟 INITIALIZING 分片
        JSONObject shard3 = new JSONObject();
        shard3.put("index", "events-2025-03");
        shard3.put("shard", "2");
        shard3.put("prirep", "p");
        shard3.put("state", "INITIALIZING");
        shard3.put("docs", "5000");
        shard3.put("store", "10.2mb");
        shard3.put("ip", "");
        shard3.put("node", "");

        shardList.add(shard1);
        shardList.add(shard2);
        shardList.add(shard3);

        return shardList;
    }

}
