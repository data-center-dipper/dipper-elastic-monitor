package com.dipper.monitor.utils.mock;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.entity.elastic.life.EsTemplateStatEntity;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadCheckItem;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadCheckResult;
import com.dipper.monitor.entity.elastic.thread.check.realtime.ThreadPoolSuggestion;

import java.util.ArrayList;
import java.util.Arrays;
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
}
