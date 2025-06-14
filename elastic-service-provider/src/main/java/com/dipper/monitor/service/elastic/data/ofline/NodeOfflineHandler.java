package com.dipper.monitor.service.elastic.data.ofline;

import com.dipper.monitor.entity.elastic.cluster.ClusterHealth;
import com.dipper.monitor.entity.elastic.original.nodes.info.EsNodeInfo;
import com.dipper.monitor.entity.elastic.shard.ShardEntity;
import com.dipper.monitor.entity.elastic.shard.recovery.AllocationEnableReq;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.service.elastic.setting.ClusterSettingService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class NodeOfflineHandler extends Thread {

    private final ElasticRealNodeService elasticRealNodeService;
    private final ElasticHealthService elasticHealthService;
    private final ElasticNodeStoreService elasticNodeStoreService;
    private final ElasticShardService elasticShardService;
    private final ClusterSettingService clusterSettingService;
    private final ElasticClientService elasticClientService;

    private final String nodeName; // 目标下线节点名
    private final StringBuilder taskLog = new StringBuilder(); // 用于记录整个任务过程日志
    private volatile boolean isDown = false; // 是否完成
    private final AtomicBoolean isRunning = new AtomicBoolean(false); // 防止重复执行

    public NodeOfflineHandler(ElasticRealNodeService elasticRealNodeService,
                              ElasticHealthService elasticHealthService,
                              ElasticNodeStoreService elasticNodeStoreService,
                              ElasticShardService elasticShardService,
                              ClusterSettingService clusterSettingService,
                              ElasticClientService elasticClientService,
                              String nodeName) {
        this.elasticRealNodeService = elasticRealNodeService;
        this.elasticHealthService = elasticHealthService;
        this.elasticNodeStoreService = elasticNodeStoreService;
        this.elasticShardService = elasticShardService;
        this.clusterSettingService = clusterSettingService;
        this.elasticClientService = elasticClientService;
        this.nodeName = nodeName;
    }

    /**
     * 外部调用前的校验逻辑
     */
    public void check(String nodeName) throws IOException {
        log("开始检查集群状态和节点信息...");

        // 检查集群健康状态
        ClusterHealth healthData = elasticHealthService.getHealthData();
        if (healthData == null) {
            throw new IllegalArgumentException("集群健康状态获取失败");
        }
        log("集群健康状态: " + healthData.getStatus());
        if (!"green".equalsIgnoreCase(healthData.getStatus())) {
            throw new IllegalArgumentException("集群状态不健康（" + healthData.getStatus() + "），无法下线节点");
        }

        // 检查节点是否存在
        List<String> nodeNameList = elasticRealNodeService.getNodeNameList();
        if (!nodeNameList.contains(nodeName)) {
            throw new IllegalArgumentException("节点不存在：" + nodeName);
        }

        // 获取节点详细信息，判断是否为主节点
        List<EsNodeInfo> esNodes = elasticRealNodeService.getEsNodes();
        for (EsNodeInfo node : esNodes) {
            if (node.getName().equals(nodeName)) {
                // 判断是否是主节点（兼容新旧版本）
                if (node.getRoles().contains("cluster_manager") || node.getRoles().contains("master")) {
                    throw new IllegalArgumentException("不能下线主节点：" + nodeName);
                }
                break;
            }
        }

        log("节点检查通过，可以安全下线。");
    }

    @Override
    public void run() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("任务已运行，请勿重复提交");
            return;
        }

        try {
            doWork();
            isDown = true;
            log("✅ 节点下线任务已完成");
        } catch (Exception e) {
            log.error("❌ 节点下线任务执行失败", e);
            log("❌ 节点下线任务执行失败: " + e.getMessage());
        }
    }

    private void doWork() throws Exception {
        log("🚀 开始执行节点下线流程");

        // 步骤1：启用所有分片分配
        AllocationEnableReq allocationEnableReq = new AllocationEnableReq();
        allocationEnableReq.setEnable("all");
        clusterSettingService.enableOrCloseShardAllocation(allocationEnableReq);
        log("✅ 已启用所有分片分配");

        // 步骤2：设置排除该节点（触发分片迁移）
        clusterSettingService.setNodeOffline(nodeName);
        log("✅ 已设置排除节点 " + nodeName + "，分片开始迁移");

        // 步骤3：持续监控分片迁移进度
        int retryCount = 0;
        final int maxRetries = 30;
        final long intervalMs = 10_000; // 10秒一次

        while (retryCount < maxRetries && !isShardsMigrated()) {
            log("⏳ 分片仍在迁移中，等待 " + intervalMs / 1000 + " 秒...");
            Thread.sleep(intervalMs);
            retryCount++;
        }

        if (retryCount >= maxRetries) {
            log.warn("⚠️ 分片迁移超时，可能仍有未迁移的分片");
        } else {
            log("✅ 分片迁移已完成");
        }

        // 步骤4：关闭节点（模拟，实际应调用外部脚本或 API）
        log("🛑 已完成分片迁移，节点可安全关闭");
    }

    private boolean isShardsMigrated() throws IOException {
        Map<String, List<ShardEntity>> stringListMap = elasticShardService.listShardMap();
        if (stringListMap == null || stringListMap.isEmpty()) {
            log("⚠️ 分片数据为空");
            return false;
        }

        for (List<ShardEntity> shards : stringListMap.values()) {
            for (ShardEntity shard : shards) {
                if (shard.getNode() != null && shard.getNode().equals(nodeName)) {
                    return false; // 还有分片在该节点上
                }
            }
        }
        return true;
    }

    public synchronized String getRecoveryState() {
        if (isDown) {
            log.info("节点下线任务已完成");
        }
        return taskLog.toString();
    }

    private void log(String message) {
        String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME);
        String logLine = "[" + time + "] " + message + "\n";
        taskLog.append(logLine);
        log.info(message);
    }

    public boolean isDown() {
        return isDown;
    }
}