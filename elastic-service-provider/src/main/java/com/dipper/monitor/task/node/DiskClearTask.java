package com.dipper.monitor.task.node;

import com.dipper.monitor.entity.db.elastic.DiskClearItem;
import com.dipper.monitor.entity.db.elastic.ElasticNodeMetricEntity;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.disk.DiskAllocationInfo;
import com.dipper.monitor.entity.elastic.disk.DiskWatermarkInfo;
import com.dipper.monitor.entity.elastic.disk.GlobalDiskClearReq;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.service.elastic.disk.DiskClearService;
import com.dipper.monitor.service.elastic.disk.ElasticDiskService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.index.IndexOneOperatorService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.nodes.NodeMetricStoreService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DiskClearTask {

    @Autowired
    private NodeMetricStoreService nodeMetricStoreService;
    @Autowired
    private ElasticRealNodeService elasticRealNodeService;
    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;
    @Autowired
    private ElasticDiskService elasticDiskService;
    @Autowired
    private DiskClearService diskClearService;
    @Autowired
    private ElasticRealIndexService elasticRealIndexService;
    @Autowired
    private IndexOneOperatorService indexOneOperatorService;

    @Scheduled(cron = "0 0 * * * ?")
    public void diskClearTask() {
        log.info("开始执行磁盘清理任务");
        try {
            // 获取磁盘信息
            List<ElasticNodeDisk> elasticNodeDisks = elasticRealNodeService.nodeDiskTop10();
            if (elasticNodeDisks == null || elasticNodeDisks.isEmpty()) {
                log.warn("未获取到磁盘信息，跳过本次清理任务");
                return;
            }

            // 获取磁盘全局配置
            GlobalDiskClearReq globalDiskClear = diskClearService.getGlobalDiskClear();
            if (globalDiskClear == null) {
                log.warn("未获取到磁盘全局配置，跳过本次清理任务");
                return;
            }

            // 获取磁盘清理配置信息
            List<DiskClearItem> diskClearItems = diskClearService.templateDiskClearAll();
            if (diskClearItems == null || diskClearItems.isEmpty()) {
                log.warn("未获取到磁盘清理配置信息，跳过本次清理任务");
                return;
            }

            // 获取索引模版信息
            List<EsTemplateEntity> allTemplates = elasticStoreTemplateService.getAllTemplates();
            if (allTemplates == null || allTemplates.isEmpty()) {
                log.warn("未获取到索引模版信息，跳过本次清理任务");
                return;
            }

            // 获取当前最高磁盘使用率
            Double maxDiskUsage = getMaxDiskUsage(elasticNodeDisks);
            log.info("当前最高磁盘使用率: {}%", maxDiskUsage);

            // 创建模板与清理配置的映射
            Map<String, DiskClearItem> templateClearMap = createTemplateClearMap(diskClearItems);

            // 根据磁盘使用率执行不同级别的清理
            if (maxDiskUsage >= globalDiskClear.getHighThreshold()) {
                // 高阈值清理 - 按优先级全部清理
                log.info("磁盘使用率达到高阈值({}%)，执行高阈值清理策略", globalDiskClear.getHighThreshold());
                executeHighThresholdClear(allTemplates, templateClearMap);
            } else if (maxDiskUsage >= globalDiskClear.getMediumThreshold()) {
                // 中阈值清理
                log.info("磁盘使用率达到中阈值({}%)，执行中阈值清理策略", globalDiskClear.getMediumThreshold());
                executeMediumThresholdClear(allTemplates, templateClearMap);
            } else if (maxDiskUsage >= globalDiskClear.getLowThreshold()) {
                // 低阈值清理
                log.info("磁盘使用率达到低阈值({}%)，执行低阈值清理策略", globalDiskClear.getLowThreshold());
                executeLowThresholdClear(allTemplates, templateClearMap);
            } else {
                log.info("当前磁盘使用率({}%)未达到清理阈值，无需清理", maxDiskUsage);
            }

            log.info("磁盘清理任务执行完成");
        } catch (Exception e) {
            log.error("磁盘清理任务异常", e);
        }
    }

    /**
     * 获取当前最高磁盘使用率
     */
    private Double getMaxDiskUsage(List<ElasticNodeDisk> elasticNodeDisks) {
        return elasticNodeDisks.stream()
                .map(ElasticNodeDisk::getDiskPercent)
                .max(Double::compare)
                .orElse(0.0);
    }

    /**
     * 创建模板名称与清理配置的映射
     */
    private Map<String, DiskClearItem> createTemplateClearMap(List<DiskClearItem> diskClearItems) {
        Map<String, DiskClearItem> templateClearMap = new HashMap<>();
        for (DiskClearItem item : diskClearItems) {
            templateClearMap.put(item.getTemplateName(), item);
        }
        return templateClearMap;
    }

    /**
     * 执行低阈值清理策略
     * 只清理配置了低阈值清理的模板索引
     */
    private void executeLowThresholdClear(List<EsTemplateEntity> allTemplates, Map<String, DiskClearItem> templateClearMap) {
        for (EsTemplateEntity template : allTemplates) {
            DiskClearItem clearItem = templateClearMap.get(template.getZhName());
            if (clearItem != null && clearItem.getMinLowThreshold() != null && clearItem.getMinLowThreshold() > 0) {
                clearTemplateIndices(template, clearItem);
            }
        }
    }

    /**
     * 执行中阈值清理策略
     * 按优先级清理配置了中阈值清理的模板索引
     */
    private void executeMediumThresholdClear(List<EsTemplateEntity> allTemplates, Map<String, DiskClearItem> templateClearMap) {
        // 按优先级排序模板
        List<EsTemplateEntity> sortedTemplates = allTemplates.stream()
                .filter(template -> {
                    DiskClearItem clearItem = templateClearMap.get(template.getEnName());
                    return clearItem != null && clearItem.getPriority() != null;
                })
                .sorted(Comparator.comparing(template -> {
                    DiskClearItem clearItem = templateClearMap.get(template.getEnName());
                    return clearItem.getPriority();
                }))
                .collect(Collectors.toList());

        // 按优先级清理
        for (EsTemplateEntity template : sortedTemplates) {
            DiskClearItem clearItem = templateClearMap.get(template.getEnName());
            clearTemplateIndices(template, clearItem);
        }
    }

    /**
     * 执行高阈值清理策略
     * 按优先级全部清理
     */
    private void executeHighThresholdClear(List<EsTemplateEntity> allTemplates, Map<String, DiskClearItem> templateClearMap) {
        // 按优先级排序所有模板
        List<EsTemplateEntity> sortedTemplates = allTemplates.stream()
                .filter(template -> templateClearMap.containsKey(template.getZhName()))
                .sorted(Comparator.comparing(template -> {
                    DiskClearItem clearItem = templateClearMap.get(template.getZhName());
                    return clearItem != null && clearItem.getPriority() != null ? clearItem.getPriority() : Integer.MAX_VALUE;
                }))
                .collect(Collectors.toList());

        // 按优先级清理所有模板的索引
        for (EsTemplateEntity template : sortedTemplates) {
            DiskClearItem clearItem = templateClearMap.get(template.getZhName());
            if (clearItem != null) {
                // 高阈值清理时，保留更少的索引
                clearTemplateIndices(template, clearItem, true);
            }
        }
    }

    /**
     * 清理模板对应的索引
     * @param template 索引模板
     * @param clearItem 清理配置
     */
    private void clearTemplateIndices(EsTemplateEntity template, DiskClearItem clearItem) {
        clearTemplateIndices(template, clearItem, false);
    }

    /**
     * 清理模板对应的索引
     * @param template 索引模板
     * @param clearItem 清理配置
     * @param isHighPriority 是否为高优先级清理（保留更少的索引）
     */
    private void clearTemplateIndices(EsTemplateEntity template, DiskClearItem clearItem, boolean isHighPriority) {
        try {
            String indexPatterns = template.getIndexPatterns();
            List<IndexEntity> indexEntities = elasticRealIndexService.listIndexNameByIndexPatterns(indexPatterns);
            if (indexEntities == null || indexEntities.isEmpty()) {
                log.info("模板 {} 没有匹配的索引，跳过清理", template.getZhName());
                return;
            }

            // 按索引大小降序排序
            indexEntities.sort(Comparator.comparing(IndexEntity::getStoreSize).reversed());

            // 确定保留的索引数量
            int retentionPeriod = clearItem.getRetentionPeriod() != null ? clearItem.getRetentionPeriod() : 7;
            // 高优先级清理时，保留更少的索引（最多保留3个或原定保留数量的一半，取较小值）
            if (isHighPriority) {
                retentionPeriod = Math.min(3, retentionPeriod / 2);
                retentionPeriod = Math.max(1, retentionPeriod); // 至少保留1个索引
            }

            // 确定最小索引大小阈值
            long minIndexSize = clearItem.getMinIndexSize() != null ? clearItem.getMinIndexSize() : 0;

            // 保留最新的N个索引，删除其余的
            List<IndexEntity> indicesToDelete = new ArrayList<>();
            for (int i = 0; i < indexEntities.size(); i++) {
                IndexEntity indexEntity = indexEntities.get(i);
                // 跳过小于最小索引大小的索引
//                if (indexEntity.getStoreSize() < minIndexSize) {
//                    continue;
//                }
                // 保留最新的N个索引
                if (i >= retentionPeriod) {
                    indicesToDelete.add(indexEntity);
                }
            }

            // 执行删除操作
            for (IndexEntity indexEntity : indicesToDelete) {
                try {
                    log.info("正在删除索引: {}, 大小: {}", indexEntity.getIndex(), indexEntity.getStoreSizeWithUnit());
                    boolean result = indexOneOperatorService.deleteIndex(indexEntity.getIndex());
                    if (result) {
                        log.info("成功删除索引: {}", indexEntity.getIndex());
                    } else {
                        log.warn("删除索引失败: {}", indexEntity.getIndex());
                    }
                } catch (Exception e) {
                    log.error("删除索引 {} 时发生异常", indexEntity.getIndex(), e);
                }
            }

            log.info("模板 {} 的索引清理完成，共删除 {} 个索引", template.getZhName(), indicesToDelete.size());
        } catch (Exception e) {
            log.error("清理模板 {} 的索引时发生异常", template.getZhName(), e);
        }
    }
}
