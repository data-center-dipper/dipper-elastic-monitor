package com.dipper.monitor.service.elastic.template.impl.handlers.shard;

import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.aware.SpringBeanAwareUtils;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.entity.elastic.template.ShardHistoryItem;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.template.impl.ElasticStoreTemplateServiceImpl;
import com.dipper.monitor.utils.UnitUtils;
import com.dipper.monitor.utils.elastic.IndexPatternsUtils;
import com.dipper.monitor.utils.mock.MockAllData;

import java.io.IOException;
import java.time.LocalDate;
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
            return MockAllData.getTemplateShardHistory(templateId);
        }
        // 获取模板信息
        EsTemplateEntity template = elasticStoreTemplateService.getTemplate(templateId);
        String indexPatterns = template.getIndexPatterns();

        // 获取符合该模板索引模式的所有索引
        List<IndexEntity> indexEntities = elasticRealIndexService.listIndexNameByIndexPatterns(indexPatterns);

        // 按日期分组聚合
        return aggregateByDate(indexEntities);
    }

    /**
     * 按日期聚合索引的分片数量与大小
     */
    private List<ShardHistoryItem> aggregateByDate(List<IndexEntity> indexEntities) {
        Map<String, ShardHistoryItem> aggregatedMap = new LinkedHashMap<>();

        for (IndexEntity index : indexEntities) {
            // 解析索引名中的日期部分（如 log-2025.05.29 -> 2025-05-29）
            String dateStr = IndexPatternsUtils.extractDateFromIndexName(index.getIndex());
            if (dateStr == null) continue;

            LocalDate date = LocalDate.parse(dateStr);
            String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE); // "2025-05-29"

            // 计算当前索引的总分片数（主分片 + 副本）
            int totalShards = index.getPri() * (index.getRep() + 1);

            // 计算当前索引的存储大小（GB）
            double shardSizeGb = UnitUtils.bytesToGB(index.getStoreSize());

            aggregatedMap.computeIfAbsent(formattedDate, k -> new ShardHistoryItem())
                    .setTimestamp(formattedDate)
                    .setShards(aggregatedMap.getOrDefault(formattedDate, new ShardHistoryItem()).getShards() + totalShards)
                    .setShardSize(
                            Math.round((aggregatedMap.getOrDefault(formattedDate, new ShardHistoryItem()).getShardSize() + shardSizeGb) * 100.0) / 100.0
                    );
        }

        return new ArrayList<>(aggregatedMap.values());
    }
}