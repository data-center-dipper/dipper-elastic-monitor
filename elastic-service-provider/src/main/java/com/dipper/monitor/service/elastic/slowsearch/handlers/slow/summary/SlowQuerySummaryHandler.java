package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.summary;

import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQuerySummaryReq;
import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQuerySummaryView;
import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQuerySummaryView.TypeDistribution;
import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQuerySummaryView.IndexCount;
import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQuerySummaryView.QueryTrend;
import com.dipper.monitor.service.elastic.slowsearch.RealSlowSearchService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryKillStoreService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class SlowQuerySummaryHandler {
    private RealSlowSearchService realSlowSearchService;
    private SlowQueryStoreService slowQueryStoreService;
    private SlowQueryKillStoreService slowQueryKillStoreService;

    public SlowQuerySummaryHandler(RealSlowSearchService realSlowSearchService, SlowQueryStoreService slowQueryStoreService, SlowQueryKillStoreService slowQueryKillStoreService) {
        this.realSlowSearchService = realSlowSearchService;
        this.slowQueryStoreService = slowQueryStoreService;
        this.slowQueryKillStoreService = slowQueryKillStoreService;
    }

    public SlowQuerySummaryView slowSearchSummary(SlowQuerySummaryReq slowQuerySummaryReq) {
        Date startTime = slowQuerySummaryReq.getStartTime();
        Date endTime = slowQuerySummaryReq.getEndTime();
        
        // 得到时间范围内所有的慢查询
        List<SlowQueryEntity> slowQueryEntities = slowQueryStoreService.queryByTimeRange(startTime, endTime);

        // 得到时间范围内的终止记录
        List<KillTimeoutRecord> killTimeoutRecords = slowQueryKillStoreService.queryByTimeRange(startTime, endTime);
        
        // 创建返回结果
        SlowQuerySummaryView summaryView = new SlowQuerySummaryView();
        
        // 1. 计算基础统计数据
        summaryView.setTotalQueries(slowQueryEntities.size());
        summaryView.setKilledQueriesCount(killTimeoutRecords.size());
        
        // 计算平均执行时间和最长执行时间
        if (!slowQueryEntities.isEmpty()) {
            OptionalDouble avgTime = slowQueryEntities.stream()
                    .filter(q -> q.getExecutionTimeMs() != null)
                    .mapToLong(SlowQueryEntity::getExecutionTimeMs)
                    .average();
            
            OptionalLong maxTime = slowQueryEntities.stream()
                    .filter(q -> q.getExecutionTimeMs() != null)
                    .mapToLong(SlowQueryEntity::getExecutionTimeMs)
                    .max();
            
            summaryView.setAvgExecutionTime(avgTime.isPresent() ? Math.round(avgTime.getAsDouble()) : 0L);
            summaryView.setMaxExecutionTime(maxTime.isPresent() ? maxTime.getAsLong() : 0L);
        } else {
            summaryView.setAvgExecutionTime(0L);
            summaryView.setMaxExecutionTime(0L);
        }
        
        // 2. 计算查询类型分布
        Map<String, Long> queryTypeCount = slowQueryEntities.stream()
                .filter(q -> q.getQueryType() != null && !q.getQueryType().isEmpty())
                .collect(Collectors.groupingBy(SlowQueryEntity::getQueryType, Collectors.counting()));
        
        List<TypeDistribution> typeDistributions = new ArrayList<>();
        queryTypeCount.forEach((type, count) -> {
            TypeDistribution distribution = new TypeDistribution();
            distribution.setName(type);
            distribution.setValue(count.intValue());
            typeDistributions.add(distribution);
        });
        summaryView.setQueryTypeDistribution(typeDistributions);
        
        // 3. 计算执行时间分布 [3-5s, 5-10s, 10-30s, 30-60s, >60s]
        int[] timeRanges = new int[5];
        for (SlowQueryEntity entity : slowQueryEntities) {
            if (entity.getExecutionTimeMs() == null) continue;
            
            long execTime = entity.getExecutionTimeMs();
            if (execTime >= 3000 && execTime < 5000) {
                timeRanges[0]++;
            } else if (execTime >= 5000 && execTime < 10000) {
                timeRanges[1]++;
            } else if (execTime >= 10000 && execTime < 30000) {
                timeRanges[2]++;
            } else if (execTime >= 30000 && execTime < 60000) {
                timeRanges[3]++;
            } else if (execTime >= 60000) {
                timeRanges[4]++;
            }
        }
        
        List<Integer> executionTimeDistribution = Arrays.stream(timeRanges).boxed().collect(Collectors.toList());
        summaryView.setExecutionTimeDistribution(executionTimeDistribution);
        
        // 4. 计算热点索引 Top 10
        Map<String, Integer> indexCountMap = new HashMap<>();
        for (SlowQueryEntity entity : slowQueryEntities) {
            if (entity.getIndexName() == null || entity.getIndexName().isEmpty()) continue;
            
            // 处理可能包含多个索引的情况（以逗号分隔）
            String[] indices = entity.getIndexName().split(",");
            for (String index : indices) {
                String trimmedIndex = index.trim();
                if (!trimmedIndex.isEmpty()) {
                    indexCountMap.put(trimmedIndex, indexCountMap.getOrDefault(trimmedIndex, 0) + 1);
                }
            }
        }
        
        // 获取前10个热点索引
        List<IndexCount> hotIndices = indexCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    IndexCount indexCount = new IndexCount();
                    indexCount.setName(entry.getKey());
                    indexCount.setValue(entry.getValue());
                    return indexCount;
                })
                .collect(Collectors.toList());
        summaryView.setHotIndices(hotIndices);
        
        // 5. 计算查询趋势
        QueryTrend queryTrend = calculateQueryTrend(slowQueryEntities, killTimeoutRecords, startTime, endTime);
        summaryView.setQueryTrend(queryTrend);
        
        return summaryView;
    }
    
    /**
     * 计算查询趋势数据
     */
    private QueryTrend calculateQueryTrend(List<SlowQueryEntity> slowQueries,
                                           List<KillTimeoutRecord> killRecords,
                                           Date startTime, Date endTime) {
        QueryTrend trend = new QueryTrend();

        // 根据时间范围划分时间点（默认8个时间点）
        long timeRange = endTime.getTime() - startTime.getTime();
        long interval = timeRange / 7; // 分成8个点，需要7个间隔

        List<String> timePoints = new ArrayList<>();
        List<Integer> slowQueriesCount = new ArrayList<>();
        List<Integer> killedQueriesCount = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        // 日期格式化器，用于将 killTime 转换为 Date
        SimpleDateFormat sdfKillTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i <= 7; i++) {
            // 计算当前时间点
            long currentTime = startTime.getTime() + (i * interval);
            Date currentDate = new Date(currentTime);
            timePoints.add(sdf.format(currentDate));

            // 计算下一个时间点（用于区间统计）
            long nextTime = (i < 7) ? startTime.getTime() + ((i + 1) * interval) : endTime.getTime();

            // 统计当前时间区间内的慢查询数量
            int slowCount = (int) slowQueries.stream()
                    .filter(q -> q.getStartTime() != null &&
                            q.getStartTime().getTime() >= currentTime &&
                            q.getStartTime().getTime() < nextTime)
                    .count();
            slowQueriesCount.add(slowCount);

            // 统计当前时间区间内的终止查询数量
            int killCount = 0;
            for (KillTimeoutRecord record : killRecords) {
                String killTimeString = record.getKillTime();
                if (killTimeString == null || killTimeString.isEmpty()) continue;

                try {
                    Date killDate = sdfKillTime.parse(killTimeString);
                    long killTimeMillis = killDate.getTime();

                    if (killTimeMillis >= currentTime && killTimeMillis < nextTime) {
                        killCount++;
                    }
                } catch (Exception e) {
                    // 解析失败跳过该记录或记录日志
                    System.err.println("Failed to parse killTime: " + killTimeString);
                }
            }
            killedQueriesCount.add(killCount);
        }

        trend.setTimePoints(timePoints);
        trend.setSlowQueries(slowQueriesCount);
        trend.setKilledQueries(killedQueriesCount);

        return trend;
    }
}
