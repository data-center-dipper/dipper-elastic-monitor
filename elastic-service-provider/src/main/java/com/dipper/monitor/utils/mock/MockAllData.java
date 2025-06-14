package com.dipper.monitor.utils.mock;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.IndexWriteEntity;
import com.dipper.monitor.entity.db.elastic.ThreadMetricEntity;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.entity.elastic.life.EsTemplateStatEntity;
import com.dipper.monitor.entity.elastic.slowsearch.task.SlowQueryTaskEntity;
import com.dipper.monitor.entity.elastic.shard.history.ShardHistoryItem;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadCheckItem;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadCheckResult;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadPoolSuggestion;
import com.dipper.monitor.entity.elastic.thread.pengding.PendingTaskView;
import com.dipper.monitor.utils.Tuple2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    public static EsTemplateStatEntity templateStat(Integer id) {
        // 创建一个新的 EsTemplateStatEntity 实例
        EsTemplateStatEntity entity = new EsTemplateStatEntity();

        // 设置一些示例数据
        entity.setId(id); // 假设 id 是从方法参数中传入的
        entity.setRollingCycleError(5); // 示例错误数
        entity.setShardCount(20); // 示例分片总数
        entity.setShardUnassigned(1); // 示例未分配分片数
        entity.setCountIndex(10); // 示例索引数量
        entity.setOpenIndex(8); // 示例打开的索引数量
        entity.setCloseIndex(2); // 示例关闭的索引数量
        entity.setExceptionIndex(0); // 示例异常索引数量
        entity.setFreezeIndex(0); // 示例冻结索引数量
        entity.setSegmetCount(150); // 示例段数量
        entity.setSegmentSize(1024L); // 示例段大小，单位为字节

        // 返回填充好的实体对象
        return entity;
    }

    public static ThreadCheckResult threadRealTimeCheck() {
        ThreadCheckResult result = new ThreadCheckResult();
        result.setOverallStatus("异常");
        result.setReadStatus("警告");
        result.setWriteStatus("正常");
        result.setMessage("部分线程池负载过高，请关注");

        // 构造 checkItems
        List<ThreadCheckItem> checkItems = Arrays.asList(
                new ThreadCheckItem()
                        .setCategory("bulk")
                        .setItem("等待队列长度")
                        .setValue("120")
                        .setThreshold("< 100")
                        .setStatus("警告")
                        .setDescription("线程池任务较多，需关注"),

                new ThreadCheckItem()
                        .setCategory("index")
                        .setItem("活跃线程比例")
                        .setValue("96%")
                        .setThreshold("< 80%")
                        .setStatus("异常")
                        .setDescription("线程池几乎满负荷运行，可能影响性能"),

                new ThreadCheckItem()
                        .setCategory("search")
                        .setItem("核心线程数")
                        .setValue("30")
                        .setThreshold(">= 20")
                        .setStatus("正常")
                        .setDescription("当前值在推荐范围内"),

                new ThreadCheckItem()
                        .setCategory("write")
                        .setItem("最大线程数")
                        .setValue("50")
                        .setThreshold(">= 40")
                        .setStatus("正常")
                        .setDescription("线程资源充足")
        );

        // 构造 suggestions
        List<ThreadPoolSuggestion> suggestions = Arrays.asList(
                new ThreadPoolSuggestion()
                        .setTitle("优化建议：调整 bulk 线程池参数")
                        .setContent("当前 bulk 线程池等待队列过长，建议增加线程数或优化任务执行效率")
                        .setActions(Arrays.asList(
                                "调整 bulk 线程池核心线程数",
                                "检查是否有慢任务阻塞线程"
                        )),

                new ThreadPoolSuggestion()
                        .setTitle("优化建议：监控 index 线程池负载")
                        .setContent("index 线程池负载接近上限，建议设置监控报警")
                        .setActions(Arrays.asList(
                                "添加线程池监控指标告警",
                                "分析任务耗时分布"
                        ))
        );

        result.setCheckItems(checkItems);
        result.setSuggestions(suggestions);

        return result;
    }

    private static final Random random = new Random();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成带有随机值的 SlowQueryTaskEntity 列表
     *
     * @return 模拟的 SlowQueryTaskEntity 列表
     */
    public static List<SlowQueryTaskEntity> getRelaSlowQuery(String clusterCode) {
        List<SlowQueryTaskEntity> mockList = new ArrayList<>();

        int count = 5; // 生成 5 条模拟数据
        LocalDateTime baseTime = LocalDateTime.now();

        String[] nodes = {"node-1", "node-2", "node-3", "node-4"};
        String[] statuses = {"completed", "running", "failed"};
        String[] actions = {"search:query", "search:scroll", "search:aggregation"};

        for (int i = 0; i < count; i++) {
            SlowQueryTaskEntity entity = new SlowQueryTaskEntity();

            // 随机字段
            String nodeId = nodes[random.nextInt(nodes.length)];
            String status = statuses[random.nextInt(statuses.length)];
            String action = actions[random.nextInt(actions.length)];

            // 查询类型
            String queryType;
            if (action.contains("scroll")) {
                queryType = "scroll";
            } else if (action.contains("aggregation")) {
                queryType = "aggregation";
            } else {
                queryType = "search";
            }

            // 执行时间（100ms - 5000ms）
            long executionTime = 100 + (Math.abs(random.nextLong()) % 4901);

            // 开始时间（当前时间往前推 0~60 分钟）
            LocalDateTime startTime = baseTime.minusMinutes(random.nextInt(60));

            // 描述信息（可模拟部分 DSL 内容）
            String description = String.format("{\"query\":{\"match_all\":{}},\"size\":%d}", random.nextInt(1000));

            entity.setClusterCode(clusterCode);
            entity.setNodeId(nodeId);
            entity.setNodeName("Node-" + nodeId.substring(nodeId.length() - 1));
            entity.setTaskId(UUID.randomUUID().toString());
            entity.setAction(action);
            entity.setQueryType(queryType);
            entity.setExecutionTime(executionTime);
            entity.setStartTime(startTime.format(formatter));
            entity.setDescription(description);
            entity.setStatus(status);
            entity.setCollectTime(new Date()); // 当前采集时间

            mockList.add(entity);
        }

        return mockList;
    }

    /**
     *     private String clusterCode;
     *     private String nodeName;
     *     private String threadType;
     *     private Integer activeThreads; 波动性
     *     private Integer queueSize; 波动性
     *     private Long rejectedCount; 自增
     *     private Long completedCount; 自增
     *     private Integer largestSize; 自增
     *     private LocalDateTime collectTime; 自增
     * @param clusterCode
     * @return
     */
    /**
     * 模拟解析线程池指标数据
     *
     * @param clusterCode 集群编码
     * @return 模拟的线程池指标列表
     */
    public static List<ThreadMetricEntity> parseThreadPoolResponse(String clusterCode) {
        List<ThreadMetricEntity> metrics = new ArrayList<>();

        // 模拟节点名称
        String[] nodeNames = {"node-1", "node-2", "node-3"};
        // 线程池类型
        String[] threadTypes = {"refresh", "search", "write", "management"};

        // 当前时间作为采集时间
        LocalDateTime collectTime = LocalDateTime.now();

        // 为每个节点和每种线程类型生成模拟数据
        for (String nodeName : nodeNames) {
            for (String threadType : threadTypes) {
                ThreadMetricEntity entity = new ThreadMetricEntity();
                entity.setClusterCode(clusterCode);
                entity.setNodeName(nodeName);
                entity.setThreadType(threadType);

                // 设置波动性字段（activeThreads, queueSize）
                entity.setActiveThreads((int) (Math.random() * 10)); // 0~9
                entity.setQueueSize((int) (Math.random() * 50));     // 0~49

                // 设置自增型字段（模拟累计值）
                entity.setRejectedCount((long) (Math.random() * 100));
                entity.setCompletedCount((long) (Math.random() * 10000));
                entity.setLargestSize((int) (Math.random() * 20));

                // 设置采集时间
                entity.setCollectTime(collectTime);

                metrics.add(entity);
            }
        }

        return metrics;
    }

    /**
     * 模拟获取模板分片历史数据（返回最近几天的固定数据）
     */
    public static List<ShardHistoryItem> getTemplateShardHistory(Integer templateId) {
        List<ShardHistoryItem> mockData = new ArrayList<>();

        // 默认返回最近 7 天的数据
        for (int i = 30; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int shards = 10 + (int)(Math.random() * 5); // 随机生成分片数
            double storeSizeGB = Math.round((50 + Math.random() * 30) * 10.0) / 10.0; // GB
            mockData.add(new ShardHistoryItem(date.toString(), shards, storeSizeGB));
        }

        return mockData;
    }

    public static List<IndexWriteEntity> writeIndexList() {
        List<IndexWriteEntity> list = new ArrayList<>();

        list.add(new IndexWriteEntity(
                "template_logs",
                "logs-2025.06.06",
                "logs_current",
                true,
                12,
                false,
                1500.5,
                890000L,
                new Date()
        ));

        list.add(new IndexWriteEntity(
                "template_users",
                "users-2025.06.06",
                "users_latest",
                false,
                8,
                true,
                300.2,
                123456L,
                new Date(System.currentTimeMillis() - 86400000) // 昨天的时间
        ));

        list.add(new IndexWriteEntity(
                "template_orders",
                "orders-2025.06.05",
                "orders_prod",
                false,
                15,
                false,
                750.0,
                567890L,
                new Date(System.currentTimeMillis() - 2 * 86400000) // 前天的时间
        ));

        return list;
    }

    public static Tuple2<Integer, List<PendingTaskView>> pendingTasks() {
        List<PendingTaskView> mockTasks = new ArrayList<>();

        // 模拟任务 1
        PendingTaskView task1 = new PendingTaskView();
        task1.setInsertOrder(123456L);
        task1.setPriority("URGENT");
        task1.setSource("create-index [logs-2025.06]");
        task1.setTimeInQueueMillis(120000L);
        task1.setExecuting(true);
        mockTasks.add(task1);

        // 模拟任务 2
        PendingTaskView task2 = new PendingTaskView();
        task2.setInsertOrder(123457L);
        task2.setPriority("HIGH");
        task2.setSource("update-mapping [users]");
        task2.setTimeInQueueMillis(80000L);
        task2.setExecuting(false);
        mockTasks.add(task2);

        // 模拟任务 3
        PendingTaskView task3 = new PendingTaskView();
        task3.setInsertOrder(123458L);
        task3.setPriority("NORMAL");
        task3.setSource("cluster_update_settings");
        task3.setTimeInQueueMillis(5000L);
        task3.setExecuting(false);
        mockTasks.add(task3);

        // 模拟任务 4
        PendingTaskView task4 = new PendingTaskView();
        task4.setInsertOrder(123459L);
        task4.setPriority("URGENT");
        task4.setSource("delete-index [old_logs]");
        task4.setTimeInQueueMillis(300000L);
        task4.setExecuting(false);
        mockTasks.add(task4);

        // 模拟任务 5
        PendingTaskView task5 = new PendingTaskView();
        task5.setInsertOrder(123460L);
        task5.setPriority("HIGH");
        task5.setSource("create-index [device_data_20250614]");
        task5.setTimeInQueueMillis(60000L);
        task5.setExecuting(true);
        mockTasks.add(task5);

        // 返回 Tuple2: 总数 + 当前页数据
        return new Tuple2<>(mockTasks.size(), mockTasks);
    }
}
