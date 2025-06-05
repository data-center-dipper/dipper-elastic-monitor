package com.dipper.monitor.service.elastic.template.impl.handlers.shard;

import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.aware.SpringBeanAwareUtils;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.entity.elastic.shard.history.SharMiddleAggItem;
import com.dipper.monitor.entity.elastic.shard.history.ShardHistoryItem;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.template.impl.ElasticStoreTemplateServiceImpl;
import com.dipper.monitor.utils.UnitUtils;
import com.dipper.monitor.utils.elastic.IndexPatternsUtils;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateShardHistoryHandler {

    private final ElasticStoreTemplateServiceImpl elasticStoreTemplateService;
    private final ElasticRealIndexService elasticRealIndexService;

    public TemplateShardHistoryHandler(ElasticStoreTemplateServiceImpl elasticStoreTemplateService) {
        this.elasticStoreTemplateService = elasticStoreTemplateService;
        this.elasticRealIndexService = SpringBeanAwareUtils.getBean(ElasticRealIndexService.class);
    }

    /**
     * 获取某个模板下的历史分片数据
     */
    public List<ShardHistoryItem> getTemplateShardHistory(Integer templateId) throws IOException {
        if(ApplicationUtils.isWindows()){
//            return MockAllData.getTemplateShardHistory(templateId);
        }
        // 获取模板信息
        EsTemplateEntity template = elasticStoreTemplateService.getTemplate(templateId);
        String indexPatterns = template.getIndexPatterns();
        String indexDatePattern = IndexPatternsUtils.getIndexDatePattern(indexPatterns);

        // 获取符合该模板索引模式的所有索引
        List<IndexEntity> indexEntities = elasticRealIndexService.listIndexNameByIndexPatterns(indexPatterns);

        // 按日期分组聚合
        return aggregateByDate(indexEntities,indexDatePattern);
    }

    /**
     * 按日期聚合索引的分片数量与大小
     */
    private List<ShardHistoryItem> aggregateByDate(List<IndexEntity> indexEntities,String indexDatePattern) {

        if (indexEntities == null || indexEntities.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<SharMiddleAggItem>> dateToIndexMap = groupIndexByDate(indexEntities, indexDatePattern);

        // 对数据排序 正序
        List<String> sortedDates = sortDate(dateToIndexMap.keySet());

        Map<String, ShardHistoryItem> aggregatedMap =  getShardHistoryItems(sortedDates,dateToIndexMap);

        return new ArrayList<>(aggregatedMap.values());
    }

    private Map<String, ShardHistoryItem> getShardHistoryItems(
            List<String> sortedDates,
            Map<String, List<SharMiddleAggItem>> dateToIndexMap) {

        Map<String, ShardHistoryItem> aggregatedMap = new LinkedHashMap<>();

        DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (String dateStr : sortedDates) {
            List<SharMiddleAggItem> items = dateToIndexMap.get(dateStr);

            if (items == null || items.isEmpty()) {
                continue;
            }

            // 按索引名排序
            items = sortIndexByName(items);

            LocalDate baseDate = LocalDate.parse(dateStr, inputFormatter);
            LocalDateTime baseTime = baseDate.atStartOfDay();

            int count = 0;
            for (SharMiddleAggItem item : items) {
                // 每个索引项增加 10 分钟
                LocalDateTime time = baseTime.plusMinutes(count * 10);
                String timestamp = time.format(outputFormatter);

                ShardHistoryItem historyItem = new ShardHistoryItem();
                historyItem.setTimestamp(timestamp);
                historyItem.setShardSize(UnitUtils.bytesToGB(item.getStoreSize()));
                historyItem.setShards(item.getPri());

                // 可选：根据需求合并相同时间点的数据（这里不合并）
                aggregatedMap.put(timestamp, historyItem);

                count++;
            }
        }

        return aggregatedMap;
    }

    private List<SharMiddleAggItem> sortIndexByName(List<SharMiddleAggItem> indexEntities) {
        if (indexEntities == null || indexEntities.isEmpty()) {
            return Collections.emptyList();
        }

        return indexEntities.stream()
                .sorted(Comparator.comparing(SharMiddleAggItem::getIndex))
                .collect(Collectors.toList());
    }

    private List<String> sortDate(Set<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> sorted = new ArrayList<>(strings);
        sorted.sort(Comparator.comparing(s -> LocalDate.parse(s)));
        return sorted;
    }

    private  Map<String, List<SharMiddleAggItem>> groupIndexByDate(List<IndexEntity> indexEntities,String indexDatePattern) {
        Map<String, List<SharMiddleAggItem>> aggregatedMap = new LinkedHashMap<>();

        for (IndexEntity index : indexEntities) {
            if (index == null) {
                continue;
            }

            // 提取日期
            String dateStr = IndexPatternsUtils.extractDateFromIndexName(index.getIndex(),indexDatePattern);
            if (dateStr == null) {
                continue;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                // 日期格式解析失败，跳过
                continue;
            }

            String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            List<SharMiddleAggItem> shardHistoryItems = aggregatedMap.get(formattedDate);
            if( shardHistoryItems == null){
                shardHistoryItems = new ArrayList<>();
            }
            SharMiddleAggItem sharMiddleAggItem = new SharMiddleAggItem();
            BeanUtils.copyProperties(index, sharMiddleAggItem);
            sharMiddleAggItem.setDateTime(formattedDate);

            shardHistoryItems.add(sharMiddleAggItem);
            aggregatedMap.put(formattedDate, shardHistoryItems);
        }

        return aggregatedMap;
    }
}