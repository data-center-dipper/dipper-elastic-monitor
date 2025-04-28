package com.dipper.monitor.task.template;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.entity.elastic.life.EsTemplateStatEntity;
import com.dipper.monitor.entity.elastic.segments.SegmentMessage;
import com.dipper.monitor.entity.elastic.shard.ShardEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.life.LifecyclePoliciesService;
import com.dipper.monitor.service.elastic.segment.ElasticSegmentService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class TemplateStatTask {

    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;
    @Autowired
    private ElasticRealTemplateService elasticRealTemplateService;
    @Autowired
    private ElasticRealIndexService elasticRealIndexService;
    @Autowired
    private LifecyclePoliciesService lifecyclePoliciesService;
    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticShardService elasticShardService;
    @Autowired
    private ElasticSegmentService elasticSegmentService;

    @QuartzJob(cron = "0 0/10 * * * ?",
            author = "hydra",
            groupName = "hydra",
            jobDesc = "elastic模板信息统计",
            editAble = true)
    public void elasticNodesUpdateTask() throws Exception {
        List<EsTemplateEntity> allTemplates = elasticStoreTemplateService.getAllTemplates();
        if (allTemplates == null || allTemplates.isEmpty()) return;

        List<EsTemplateStatEntity> templateStats = allTemplates.stream()
                .map(this::processTemplate)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        elasticStoreTemplateService.updateTemplateStat(templateStats);
    }

    private EsTemplateStatEntity processTemplate(EsTemplateEntity templateEntity) {
        long startTime = System.currentTimeMillis();
        log.info("开始处理模板: {}", templateEntity.getId());

        List<String> indexPatternList = getIndexPatternList(templateEntity);
        if (indexPatternList.isEmpty()) return null;

        Set<EsLifeCycleManagement> lifeCycleErrors = collectLifecycleErrors(indexPatternList);
        Map<String, IndexEntity> indexAllMap = collectIndexEntities(indexPatternList);
        Set<ShardEntity> shardEntities = collectShardEntities(indexPatternList);
        Set<SegmentMessage> segmentMessages = collectSegmentMessages(indexPatternList);

        EsTemplateStatEntity statEntity = aggregateStatistics(templateEntity, lifeCycleErrors, indexAllMap, shardEntities, segmentMessages);
        log.info("完成处理模板 {}，耗时：{}ms", templateEntity.getId(), System.currentTimeMillis() - startTime);

        return statEntity;
    }

    private List<String> getIndexPatternList(EsTemplateEntity templateEntity) {
        return elasticRealTemplateService.getIndexPatternList(templateEntity.getIndexPatterns());
    }

    private Set<EsLifeCycleManagement> collectLifecycleErrors(List<String> patterns) {
        Set<EsLifeCycleManagement> errors = new HashSet<>();
        for (String pattern : patterns) {
            try {
                lifecyclePoliciesService.getLifeCycleExList(pattern + "*").stream()
                        .filter(item -> item.getIndex().startsWith(getIndexPrefix(pattern)))
                        .forEach(errors::add);
            } catch (IOException e) {
                log.error("获取生命周期异常", e);
            }
        }
        return errors;
    }

    private Map<String, IndexEntity> collectIndexEntities(List<String> patterns) {
        return patterns.stream().flatMap(pattern -> {
            try {
                return elasticRealIndexService.listIndexNameByPrefix(getIndexPrefix(pattern), pattern + "*").stream();
            } catch (IOException e) {
                log.error("获取索引列表失败", e);
                return Stream.empty();
            }
        }).collect(Collectors.toMap(IndexEntity::getIndex, Function.identity()));
    }

    private Set<ShardEntity> collectShardEntities(List<String> patterns) {
        Set<ShardEntity> shards = new HashSet<>();
        patterns.forEach(pattern -> {
            try {
                shards.addAll(elasticShardService.listShardByPrefix(getIndexPrefix(pattern), pattern + "*"));
            } catch (IOException e) {
                log.error("获取shard失败：{}", e.getMessage());
            }
        });
        return shards;
    }

    private Set<SegmentMessage> collectSegmentMessages(List<String> patterns) {
        Set<SegmentMessage> segments = new HashSet<>();
        patterns.forEach(pattern -> {
            try {
                segments.addAll(elasticSegmentService.listSegmentByPrefix(getIndexPrefix(pattern), pattern + "*"));
            } catch (IOException e) {
                log.error("获取segment失败", e);
            }
        });
        return segments;
    }

    private EsTemplateStatEntity aggregateStatistics(EsTemplateEntity templateEntity, Set<EsLifeCycleManagement> lifeCycleErrors, Map<String, IndexEntity> indices, Set<ShardEntity> shards, Set<SegmentMessage> segments) {
        int openIndex = 0;
        int closeIndex = 0;
        int exceptionIndex = 0;
        int freezeIndex = 0;
        AtomicInteger shardUnassigned = new AtomicInteger();
        AtomicLong segmentSize = new AtomicLong(0L);

        for (Map.Entry<String, IndexEntity> entry : indices.entrySet()) {
            IndexEntity index = entry.getValue();
            String status = index.getStatus();

            if ("open".equals(status)) openIndex++;
            else if ("close".equals(status)) closeIndex++;

            if ("red".equalsIgnoreCase(index.getHealth()) || "yellow".equalsIgnoreCase(index.getHealth())) exceptionIndex++;

            if (index.getSettings() != null && index.getSettings().contains("\"frozen\":\"true\"")) freezeIndex++;

            shards.stream().filter(shard -> shard.getIndex().equals(entry.getKey())).forEach(shard -> {
                if ("UNASSIGNED".equalsIgnoreCase(shard.getState())) shardUnassigned.getAndIncrement();
            });

            segments.stream().filter(segment -> segment.getIndex().equals(entry.getKey())).forEach(segment -> segmentSize.addAndGet(segment.getSizeMemory()));
        }

        EsTemplateStatEntity statEntity = new EsTemplateStatEntity();
        BeanUtils.copyProperties(templateEntity, statEntity);
        statEntity.setRollingCycleError(lifeCycleErrors.size());
        statEntity.setOpenIndex(openIndex);
        statEntity.setCloseIndex(closeIndex);
        statEntity.setExceptionIndex(exceptionIndex);
        statEntity.setFreezeIndex(freezeIndex);
        statEntity.setShardCount(shards.size());
        statEntity.setShardUnassigned(shardUnassigned.get());
        statEntity.setSegmetCount(segments.size());
        statEntity.setSegmentSize(segmentSize.get() / 1048576L);

        return statEntity;
    }

    private String getIndexPrefix(String indexPattern) {
        int pos = indexPattern.replaceAll("[^\\d_]*([\\d_]+)$", "$1").length();
        return indexPattern.substring(0, indexPattern.length() - pos);
    }

}
