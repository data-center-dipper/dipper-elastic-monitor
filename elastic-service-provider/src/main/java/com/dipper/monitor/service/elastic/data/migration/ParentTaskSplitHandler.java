package com.dipper.monitor.service.elastic.data.migration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.aware.SpringBeanAwareUtils;
import com.dipper.monitor.entity.db.elastic.ElasticClusterEntity;
import com.dipper.monitor.entity.db.elastic.SunTaskEntity;
import com.dipper.monitor.entity.elastic.cluster.ClusterHealth;
import com.dipper.monitor.entity.elastic.cluster.ElasticClusterView;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskReq;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ParentTaskSplitHandler {

    private final ElasticClientService elasticClientService;
    private final ElasticRealIndexService elasticRealIndexService;
    private  ElasticHealthService elasticHealthService;

    public ParentTaskSplitHandler(ElasticClientService elasticClientService, ElasticRealIndexService elasticRealIndexService) {
        this.elasticClientService = elasticClientService;
        this.elasticRealIndexService = elasticRealIndexService;
    }

    /**
     * 拆分子任务
     *
     * @param taskReq 用户提交的任务请求
     * @return 子任务列表
     */
    public List<SunTaskEntity> splitTask(MigrationTaskReq taskReq) throws IOException {
        // 1. 获取匹配索引列表
        String indexPattern = taskReq.getIndexPattern();
        String indexPrefix = indexPattern.replace("*", "");
        List<IndexEntity> indexEntities = elasticRealIndexService.listIndexNameByPrefix(indexPrefix, indexPattern);
        if (CollectionUtils.isEmpty(indexEntities)) {
            log.warn("未找到匹配的索引: {}", indexPattern);
            return Collections.emptyList();
        }
        log.info("匹配到的索引列表: {}", indexEntities.size());
        // 时间取值只有 1小时  24小时
        long granularityHours = calculateGranularityInHours(taskReq);

        // 3. 解析用户查询条件
        JSONObject originalQuery = parseUserQuery(taskReq.getQueryCondition());

        String taskId = taskReq.getTaskId();
        // 4. 遍历每个索引 + 时间段组合，生成子任务
        List<SunTaskEntity> subTasks = new ArrayList<>();
        for (IndexEntity index : indexEntities) {
                // 构建子任务
                SunTaskEntity subTask = new SunTaskEntity();
                subTask.setParentTaskId(taskId);
                subTask.setIndexName(index.getIndex());
                subTask.setQueryContent(originalQuery.toJSONString());
                subTask.setStatus("PENDING");
                subTask.setRetryCount(0);
                subTask.setStartTime(LocalDateTime.now());
                subTask.setEndTime(LocalDateTime.now());
                subTask.setCreatedAt(LocalDateTime.now());
                subTask.setUpdatedAt(LocalDateTime.now());

                subTasks.add(subTask);
        }

        return subTasks;
    }

    // 计算粒度为小时数
    private long calculateGranularityInHours(MigrationTaskReq taskReq) {
        String granularity = taskReq.getGranularity();
        if ("hourly".equalsIgnoreCase(granularity)) {
            return 1;
        } else if ("daily".equalsIgnoreCase(granularity)) {
            return 24;
        }
        return 1;
    }

    // 解析用户原始查询条件
    private JSONObject parseUserQuery(String queryJson) {
        if (StringUtils.isBlank(queryJson)) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(queryJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的查询JSON格式：" + queryJson, e);
        }
    }


    /**
     * 校验迁移任务请求参数合法性
     *
     * @param taskReq 请求参数对象
     * @throws IllegalArgumentException 参数不合法时抛出异常
     */
    public void checkParams(MigrationTaskReq taskReq) throws IllegalArgumentException {
        // 1. 校验源集群和目标集群 ID 是否为空
        if (StringUtils.isBlank(taskReq.getTaskId())) {
            throw new IllegalArgumentException("任务 ID 不能为空");
        }
        if (StringUtils.isBlank(taskReq.getSourceClusterId())) {
            throw new IllegalArgumentException("源集群 ID 不能为空");
        }
        if (StringUtils.isBlank(taskReq.getTargetClusterId())) {
            throw new IllegalArgumentException("目标集群 ID 不能为空");
        }

        // 2. 获取所有集群信息，用于后续校验
        ElasticClusterManagerService elasticClusterManagerService =
                SpringBeanAwareUtils.getBean(ElasticClusterManagerService.class);
        List<ElasticClusterView> allClusters = elasticClusterManagerService.getAllCluster();

        if (CollectionUtils.isEmpty(allClusters)) {
            throw new IllegalArgumentException("当前无可用集群，请先配置集群信息");
        }

        // 3. 判断源集群和目标集群是否存在
        ElasticClusterEntity sourceCluster = elasticClusterManagerService.getClusterById(taskReq.getSourceClusterId());
        ElasticClusterEntity targetCluster = elasticClusterManagerService.getClusterById(taskReq.getTargetClusterId());

        if (sourceCluster == null) {
            throw new IllegalArgumentException("源集群不存在，ID: " + taskReq.getSourceClusterId());
        }
        if (targetCluster == null) {
            throw new IllegalArgumentException("目标集群不存在，ID: " + taskReq.getTargetClusterId());
        }

        // 4. 源集群不能等于目标集群
//        if (sourceCluster.getClusterCode().equals(targetCluster.getClusterCode())) {
//            throw new IllegalArgumentException("源集群和目标集群不能相同");
//        }

        // 5. 校验索引模式是否为空
        if (StringUtils.isBlank(taskReq.getIndexPattern())) {
            throw new IllegalArgumentException("索引匹配模式不能为空");
        }

        // 6. 校验粒度字段是否合法
        String granularity = taskReq.getGranularity();
        if (!Arrays.asList("hourly", "daily","day", "custom").contains(granularity)) {
            throw new IllegalArgumentException("迁移粒度必须是 hourly/daily/custom 中的一种");
        }
        // 7. 校验执行策略是否合法
        String executePolicy = taskReq.getExecutePolicy();
        if (!Arrays.asList("abort", "continue","continue_on_error").contains(executePolicy)) {
            throw new IllegalArgumentException("执行策略必须为 abort 或 continue");
        }

        // 8. 校验并发限制是否合法
        Integer concurrencyLimit = taskReq.getConcurrencyLimit();
        if (concurrencyLimit == null || concurrencyLimit < 1) {
            throw new IllegalArgumentException("并发限制必须大于等于 1");
        }

        // 9. 如果有查询条件，尝试解析 JSON 格式是否正确
        if (StringUtils.isNotBlank(taskReq.getQueryCondition())) {
            try {
                JSON.parseObject(taskReq.getQueryCondition());
            } catch (JSONException e) {
                throw new IllegalArgumentException("查询条件 JSON 格式错误：" + e.getMessage(), e);
            }
        }
    }

    /**
     * 根据 clusterCode 查找集群信息
     *
     * @param clusters 集群列表
     * @param clusterId 要查找的 clusterCode（不是数据库 id）
     * @return 匹配的集群信息，未找到返回 null
     */
    private ElasticClusterView findClusterById(List<ElasticClusterView> clusters, String clusterId) {
        if (CollectionUtils.isEmpty(clusters) || StringUtils.isBlank(clusterId)) {
            return null;
        }
        return clusters.stream()
                .filter(cluster -> cluster.getId().equals(clusterId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 校验运行时参数（集群连接性和健康状态）
     *
     * @param taskReq 请求参数对象
     * @throws RuntimeException 如果集群不可用或状态不健康
     */
    public void checkRunParams(MigrationTaskReq taskReq) {
        String sourceClusterId = taskReq.getSourceClusterId();
        String targetClusterId = taskReq.getTargetClusterId();

        ElasticClusterManagerService elasticClusterManagerService =
                SpringBeanAwareUtils.getBean(ElasticClusterManagerService.class);

        // 获取源集群实体
        ElasticClusterEntity sourceCluster = elasticClusterManagerService.getClusterById(sourceClusterId);
        if (sourceCluster == null) {
            throw new RuntimeException("源集群不存在，ID: " + sourceClusterId);
        }

        // 获取目标集群实体
        ElasticClusterEntity targetCluster = elasticClusterManagerService.getClusterById(targetClusterId);
        if (targetCluster == null) {
            throw new RuntimeException("目标集群不存在，ID: " + targetClusterId);
        }

        // 注入服务（假设你有 ClusterHealthService 来获取集群健康状态）
        ElasticHealthService elasticHealthService = SpringBeanAwareUtils.getBean(ElasticHealthService.class);

        // 检查源集群健康状态
        ClusterHealth sourceHealth = elasticHealthService.getClusterHealthData(sourceCluster);
        if (sourceHealth == null) {
            throw new RuntimeException("无法连接源集群：" + sourceCluster.getClusterName());
        }
//        if (!"green".equalsIgnoreCase(sourceHealth.getStatus())) {
//            throw new RuntimeException("源集群 [" + sourceCluster.getClusterName() + "] 状态不正常，当前状态：" + sourceHealth.getStatus());
//        }

        // 检查目标集群健康状态
        ClusterHealth targetHealth = elasticHealthService.getClusterHealthData(targetCluster);
        if (targetHealth == null) {
            throw new RuntimeException("无法连接目标集群：" + targetCluster.getClusterName());
        }
//        if (!"green".equalsIgnoreCase(targetHealth.getStatus())) {
//            throw new RuntimeException("目标集群 [" + targetCluster.getClusterName() + "] 状态不正常，当前状态：" + targetHealth.getStatus());
//        }
    }


}