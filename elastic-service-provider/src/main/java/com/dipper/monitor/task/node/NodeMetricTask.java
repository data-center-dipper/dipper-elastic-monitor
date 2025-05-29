package com.dipper.monitor.task.node;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDetail;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
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
public class NodeMetricTask {
    
    @Autowired
    private ElasticClientService elasticClientService;
    
    @Autowired
    private ElasticRealNodeService elasticRealNodeService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 每5分钟执行一次节点指标收集任务
     */
    @Scheduled(cron = "0 */5 * * * ?")
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
            
            // 收集节点网络和IO指标
            collectNodeNetworkAndIOMetrics(currentCluster);
            
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
        
        List<Map<String, Object>> batchParams = new ArrayList<>();
        
        nodes.forEach((nodeId, value) -> {
            JSONObject nodeJson = (JSONObject) value;
            // 使用ListHighMemoryRiskNodesHandler中的静态方法，不设置风险阈值
            ElasticNodeDetail nodeDetail = ListHighMemoryRiskNodesHandler.buildElasticNodeDetail(nodeJson, false);
            
            // 提取网络和IO指标
            JSONObject fsStats = nodeJson.getJSONObject("fs");
            JSONObject networkStats = nodeJson.getJSONObject("transport");
            
            Map<String, Object> params = new HashMap<>();
            params.put("cluster_code", currentCluster.getClusterCode());
            params.put("node_id", nodeId);
            params.put("node_name", nodeDetail.getName());
            params.put("host_ip", nodeDetail.getHostIp());
            params.put("transport_address", nodeDetail.getTransportAddress());
            params.put("roles", nodeDetail.getRoles());
            
            // CPU指标
            params.put("cpu_percent", nodeDetail.getCpuPercent());
            
            // 内存指标
            params.put("os_mem_total", nodeDetail.getOsMemTotal());
            params.put("os_mem_free", nodeDetail.getOsMemFree());
            params.put("os_mem_used", nodeDetail.getOsMemUsed());
            params.put("os_mem_used_percent", nodeDetail.getOsMemusedPercent());
            params.put("os_mem_free_percent", nodeDetail.getOsMemFreePercent());
            params.put("jvm_mem_heap_used", nodeDetail.getJvmMemHeapused());
            params.put("jvm_mem_heap_used_percent", nodeDetail.getJvmMemHeapusedPrecent());
            params.put("jvm_mem_heap_max", nodeDetail.getJvmMemHeapMax());
            
            // 文件描述符
            params.put("open_file_descriptors", nodeDetail.getOpenFileDescriptors());
            params.put("max_file_descriptors", nodeDetail.getMaxFileDescriptors());
            
            // 线程指标
            params.put("threads_count", nodeDetail.getThreadsCount());
            
            // 网络指标
            if (networkStats != null) {
                params.put("network_rx_size", getSafeLongFromNestedJson(nodeJson, "transport.rx_size_in_bytes"));
                params.put("network_tx_size", getSafeLongFromNestedJson(nodeJson, "transport.tx_size_in_bytes"));
            }
            
            // IO指标
            if (fsStats != null) {
                JSONObject ioStats = fsStats.getJSONObject("io_stats");
                if (ioStats != null) {
                    params.put("io_read_operations", getSafeLongFromNestedJson(ioStats, "total.read_operations"));
                    params.put("io_write_operations", getSafeLongFromNestedJson(ioStats, "total.write_operations"));
                    params.put("io_read_size", getSafeLongFromNestedJson(ioStats, "total.read_kilobytes"));
                    params.put("io_write_size", getSafeLongFromNestedJson(ioStats, "total.write_kilobytes"));
                }
            }
            
            params.put("collect_time", LocalDateTime.now().format(FORMATTER));
            batchParams.add(params);
        });
        
        // 批量插入数据库
        if (!batchParams.isEmpty()) {
            batchInsertNodeMetrics(batchParams);
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
        
        List<Map<String, Object>> batchParams = new ArrayList<>();
        
        for (Map.Entry<String, ElasticNodeDisk> entry : nodeDiskMap.entrySet()) {
            String nodeName = entry.getKey();
            ElasticNodeDisk diskInfo = entry.getValue();
            
            // 查询已有记录
            String sql = "SELECT id FROM t_elastic_node_metric WHERE cluster_code = ? AND node_name = ? AND collect_time = ?";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, 
                    currentCluster.getClusterCode(), nodeName, LocalDateTime.now().format(FORMATTER));
            
            if (!results.isEmpty()) {
                // 更新已有记录
                for (Map<String, Object> result : results) {
                    Long id = (Long) result.get("id");
                    String updateSql = "UPDATE t_elastic_node_metric SET disk_total = ?, disk_used = ?, disk_avail = ?, disk_percent = ?, shards_count = ? WHERE id = ?";
                    jdbcTemplate.update(updateSql, 
                            diskInfo.getDiskTotal(),
                            diskInfo.getDiskUsed(),
                            diskInfo.getDiskAvail(),
                            diskInfo.getDiskPercent(),
                            diskInfo.getShards(),
                            id);
                }
            } else {
                // 创建新记录
                Map<String, Object> params = new HashMap<>();
                params.put("cluster_code", currentCluster.getClusterCode());
                params.put("node_name", nodeName);
                params.put("disk_total", diskInfo.getDiskTotal());
                params.put("disk_used", diskInfo.getDiskUsed());
                params.put("disk_avail", diskInfo.getDiskAvail());
                params.put("disk_percent", diskInfo.getDiskPercent());
                params.put("shards_count", diskInfo.getShards());
                params.put("collect_time", LocalDateTime.now().format(FORMATTER));
                batchParams.add(params);
            }
        }
        
        // 批量插入数据库
        if (!batchParams.isEmpty()) {
            batchInsertNodeMetrics(batchParams);
        }
    }
    
    /**
     * 收集节点网络和IO指标
     */
    private void collectNodeNetworkAndIOMetrics(CurrentClusterEntity currentCluster) throws IOException {
        // 这部分指标已在collectNodeMemoryAndCpuMetrics方法中收集
    }
    
    /**
     * 批量插入节点指标数据
     */
    private void batchInsertNodeMetrics(List<Map<String, Object>> batchParams) {
        if (batchParams.isEmpty()) {
            return;
        }
        
        // 构建批量插入SQL
        StringBuilder sql = new StringBuilder("INSERT INTO t_elastic_node_metric (");
        StringBuilder placeholders = new StringBuilder("VALUES (");
        
        // 获取第一个参数的所有键作为列名
        Map<String, Object> firstParam = batchParams.get(0);
        List<String> columns = new ArrayList<>(firstParam.keySet());
        
        for (int i = 0; i < columns.size(); i++) {
            sql.append(columns.get(i));
            placeholders.append("?");
            
            if (i < columns.size() - 1) {
                sql.append(", ");
                placeholders.append(", ");
            }
        }
        
        sql.append(") ").append(placeholders).append(")");
        
        // 执行批量插入
        jdbcTemplate.batchUpdate(sql.toString(), batchParams, batchParams.size(),
                (ps, argument) -> {
                    int i = 1;
                    for (String column : columns) {
                        ps.setObject(i++, argument.get(column));
                    }
                });
        
        log.info("成功批量插入{}条节点指标数据", batchParams.size());
    }
    
    /**
     * 构建节点详情对象
     */
    // 删除原有的buildElasticNodeDetail方法
    // private ElasticNodeDetail buildElasticNodeDetail(JSONObject nodeJson) { ... }
    
    // 删除所有辅助方法，因为它们已经在ListHighMemoryRiskNodesHandler中定义为静态方法
    // 如果这些方法在其他地方也有使用，可以保留，或者考虑创建一个单独的工具类
    
    // 辅助方法用于安全获取JSON中的值
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
}
