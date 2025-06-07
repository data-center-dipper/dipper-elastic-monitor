package com.dipper.monitor.task.node;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.ElasticNodeMetricEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDetail;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.nodes.NodeMetricStoreService;
import com.dipper.monitor.task.AbstractITask;
import com.dipper.monitor.task.ITask;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// 添加导入语句
import com.dipper.monitor.service.elastic.nodes.impl.handlers.ListHighMemoryRiskNodesHandler;

/**
 * ES节点指标监控定时任务
 * 负责收集ES节点的各项指标并存储到数据库
 */
@Slf4j
@Component
public class NodeMetricTask  extends AbstractITask  {
    
    @Autowired
    private ElasticClientService elasticClientService;
    
    @Autowired
    private ElasticRealNodeService elasticRealNodeService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private NodeMetricStoreService nodeMetricStoreService; // 添加新的服务依赖
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 每5分钟执行一次节点指标收集任务
     */
    public void collectNodeMetrics() {
        log.info("开始收集ES节点指标数据: {}", LocalDateTime.now().format(FORMATTER));
        try {
            // 获取当前集群信息
            CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
            if (currentCluster == null) {
                log.warn("未找到当前集群信息，无法执行节点指标收集");
                return;
            }
            
            // 收集节点内存和CPU指标
            collectNodeMemoryAndCpuMetrics(currentCluster);
            
            // 收集节点磁盘指标
            collectNodeDiskMetrics(currentCluster);
            
            log.info("ES节点指标数据收集完成: {}", LocalDateTime.now().format(FORMATTER));
        } catch (Exception e) {
            log.error("收集ES节点指标数据失败", e);
        }
    }
    
    /**
     * 收集节点内存和CPU指标
     */
    private void collectNodeMemoryAndCpuMetrics(CurrentClusterEntity currentCluster) throws IOException {
        String nodeInfoResult = elasticClientService.executeGetApi(ElasticRestApi.ES_NODES_STAT_MESSAGE.getApiPath());
        if (StringUtils.isBlank(nodeInfoResult) || nodeInfoResult.contains("master_not_discovered_exception")) {
            log.warn("获取节点统计信息失败");
            return;
        }
        
        JSONObject jsonObject = JSON.parseObject(nodeInfoResult);
        JSONObject nodes = jsonObject.getJSONObject("nodes");
        
        List<ElasticNodeMetricEntity> metricEntities = new ArrayList<>();
        
        nodes.forEach((nodeId, value) -> {
            JSONObject nodeJson = (JSONObject) value;
            // 使用ListHighMemoryRiskNodesHandler中的静态方法，不设置风险阈值
            ElasticNodeDetail nodeDetail = ListHighMemoryRiskNodesHandler.buildElasticNodeDetail(nodeJson, false);
            
            // 提取网络和IO指标
            JSONObject fsStats = nodeJson.getJSONObject("fs");
            JSONObject networkStats = nodeJson.getJSONObject("transport");
            
            ElasticNodeMetricEntity entity = new ElasticNodeMetricEntity();
            entity.setClusterCode(currentCluster.getClusterCode());
            entity.setNodeId(nodeId);
            entity.setNodeName(nodeDetail.getName());
            entity.setHostIp(nodeDetail.getHostIp());
            entity.setTransportAddress(nodeDetail.getTransportAddress());
            entity.setRoles(nodeDetail.getRoles());
            
            // CPU指标
            entity.setCpuPercent(nodeDetail.getCpuPercent());
            
            // 内存指标
            entity.setOsMemTotal(nodeDetail.getOsMemTotal());
            entity.setOsMemFree(nodeDetail.getOsMemFree());
            entity.setOsMemUsed(nodeDetail.getOsMemUsed());
            entity.setOsMemUsedPercent(nodeDetail.getOsMemusedPercent());
            entity.setOsMemFreePercent(nodeDetail.getOsMemFreePercent());
            entity.setJvmMemHeapUsed(nodeDetail.getJvmMemHeapused());
            entity.setJvmMemHeapUsedPercent(nodeDetail.getJvmMemHeapusedPrecent());
            entity.setJvmMemHeapMax(nodeDetail.getJvmMemHeapMax());
            
            // 文件描述符
            entity.setOpenFileDescriptors(nodeDetail.getOpenFileDescriptors());
            entity.setMaxFileDescriptors(nodeDetail.getMaxFileDescriptors());
            
            // 线程指标
            entity.setThreadsCount(nodeDetail.getThreadsCount());
            
            // 网络指标
            if (networkStats != null) {
                entity.setNetworkRxSize(getSafeLongFromNestedJson(nodeJson, "transport.rx_size_in_bytes"));
                entity.setNetworkTxSize(getSafeLongFromNestedJson(nodeJson, "transport.tx_size_in_bytes"));
            }
            
            // IO指标
            if (fsStats != null) {
                JSONObject ioStats = fsStats.getJSONObject("io_stats");
                if (ioStats != null) {
                    entity.setIoReadOperations(getSafeLongFromNestedJson(ioStats, "total.read_operations"));
                    entity.setIoWriteOperations(getSafeLongFromNestedJson(ioStats, "total.write_operations"));
                    entity.setIoReadSize(getSafeLongFromNestedJson(ioStats, "total.read_kilobytes"));
                    entity.setIoWriteSize(getSafeLongFromNestedJson(ioStats, "total.write_kilobytes"));
                }
            }
            
            entity.setCollectTime(LocalDateTime.now());
            metricEntities.add(entity);
        });
        
        // 使用新的服务接口批量保存节点指标
        if (!metricEntities.isEmpty()) {
            int savedCount = nodeMetricStoreService.batchSaveNodeMetrics(metricEntities);
            log.info("成功保存{}条节点指标数据", savedCount);
        }
    }
    
    /**
     * 收集节点磁盘指标
     */
    private void collectNodeDiskMetrics(CurrentClusterEntity currentCluster) throws IOException {
        Map<String, ElasticNodeDisk> nodeDiskMap = elasticRealNodeService.getEsNodeDiskMap();
        if (nodeDiskMap.isEmpty()) {
            log.warn("获取节点磁盘信息失败");
            return;
        }
        
        // 获取当前集群所有节点的最新指标
        List<ElasticNodeMetricEntity> latestMetrics = nodeMetricStoreService.getLatestNodeMetrics(currentCluster.getClusterCode());
        Map<String, ElasticNodeMetricEntity> nodeMetricMap = new HashMap<>();
        for (ElasticNodeMetricEntity metric : latestMetrics) {
            nodeMetricMap.put(metric.getNodeName(), metric);
        }
        
        List<ElasticNodeMetricEntity> updatedMetrics = new ArrayList<>();
        
        for (Map.Entry<String, ElasticNodeDisk> entry : nodeDiskMap.entrySet()) {
            String nodeName = entry.getKey();
            ElasticNodeDisk diskInfo = entry.getValue();
            
            ElasticNodeMetricEntity existingMetric = nodeMetricMap.get(nodeName);
            if (existingMetric != null) {
                // 更新已有记录的磁盘信息
                existingMetric.setDiskTotal(diskInfo.getDiskTotal());
                existingMetric.setDiskUsed(diskInfo.getDiskUsed());
                existingMetric.setDiskAvail(diskInfo.getDiskAvail());
                existingMetric.setDiskPercent(diskInfo.getDiskPercent());
                existingMetric.setShardsCount(diskInfo.getShards());
                updatedMetrics.add(existingMetric);
            } else {
                // 创建新记录
                ElasticNodeMetricEntity newMetric = new ElasticNodeMetricEntity();
                newMetric.setClusterCode(currentCluster.getClusterCode());
                newMetric.setNodeName(nodeName);
                newMetric.setDiskTotal(diskInfo.getDiskTotal());
                newMetric.setDiskUsed(diskInfo.getDiskUsed());
                newMetric.setDiskAvail(diskInfo.getDiskAvail());
                newMetric.setDiskPercent(diskInfo.getDiskPercent());
                newMetric.setShardsCount(diskInfo.getShards());
                newMetric.setCollectTime(LocalDateTime.now());
                updatedMetrics.add(newMetric);
            }
        }
        
        // 使用新的服务接口批量保存更新后的节点指标
        if (!updatedMetrics.isEmpty()) {
            int savedCount = nodeMetricStoreService.batchSaveNodeMetrics(updatedMetrics);
            log.info("成功更新{}条节点磁盘指标数据", savedCount);
        }
    }
    
    // 删除原有的collectNodeNetworkAndIOMetrics方法，因为这部分指标已在collectNodeMemoryAndCpuMetrics方法中收集
    
    // 删除原有的batchInsertNodeMetrics方法，使用新的服务接口代替
    
    // 保留辅助方法用于安全获取JSON中的值
    private String getSafeString(JSONObject json, String key) {
        return Optional.ofNullable(json.getString(key)).orElse("");
    }
    
    private Integer getSafeInteger(JSONObject json, String key) {
        return Optional.ofNullable(json.getInteger(key)).orElse(null);
    }
    
    private Long getSafeLong(JSONObject json, String key) {
        return Optional.ofNullable(json.getLong(key)).orElse(null);
    }
    
    private String getSafeArrayAsString(JSONObject json, String key) {
        JSONArray array = json.getJSONArray(key);
        return array != null ? array.toJavaList(String.class).toString() : "";
    }
    
    private Integer getSafeIntegerFromNestedJson(JSONObject json, String nestedKey) {
        String[] keys = nestedKey.split("\\.");
        Object value = json;
        for (String k : keys) {
            if (!(value instanceof JSONObject)) break;
            value = ((JSONObject) value).get(k);
        }
        return value instanceof Integer ? (Integer) value : null;
    }
    
    private Long getSafeLongFromNestedJson(JSONObject json, String nestedKey) {
        String[] keys = nestedKey.split("\\.");
        Object value = json;
        for (String k : keys) {
            if (!(value instanceof JSONObject)) break;
            value = ((JSONObject) value).get(k);
        }
        return value instanceof Long ? (Long) value : null;
    }
    
    private Double getSizeInGB(Long bytes) {
        return bytes != null ? bytes / 1073741824.0 : null;
    }

    @Override
    public String getCron() {
        return "0 */5 * * * ?";
    }

    @Override
    public void setCron(String cron) {

    }

    @Override
    public String getAuthor() {
        return "lcc";
    }

    @Override
    public String getJobDesc() {
        return "节点指标收集器";
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void execute() {
        collectNodeMetrics();
    }

    @Override
    public String getTaskName() {
        return "collectNodeMetrics";
    }
}
