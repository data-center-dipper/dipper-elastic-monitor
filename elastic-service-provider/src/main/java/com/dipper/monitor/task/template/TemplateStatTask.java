package com.dipper.monitor.task.template;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.entity.elastic.life.EsTemplateConfigMes;
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
import java.util.function.Function;
import java.util.stream.Collectors;

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
        if (allTemplates == null || allTemplates.isEmpty()) {
            return;
        }
        List<EsTemplateStatEntity> templateStat = new ArrayList<>();
        for (EsTemplateEntity templateEntity : allTemplates) {
            EsTemplateStatEntity esTemplateConfigMes = staticTemplate(templateEntity);
            if(esTemplateConfigMes != null) {
                continue;
            }
            templateStat.add(esTemplateConfigMes);
        }
        // todo: 将结果存储到数据库
        elasticStoreTemplateService.updateTemplateStat(templateStat);
    }

    /**
     * 计算一个模板下有多少个索引，开启的有多少个 关闭的有多少个 冻结的有多少个 异常的有多少个
     * 分片数有多少个，段有多少个
     * 占用的总jmv信息有多少
     * <p>
     * 索引：共 35个，关闭 4个 ，冻结 - ，开启 31个
     * 滚动周期：日
     * 异常索引个数：-，分片个数：268个 ，异常分片个数：-
     * 段：共 399个 ，JVM使用： 5MB
     * <p>
     * 这些统计信息
     *
     * @param templateEntity 模板实体
     * @return
     */
    private EsTemplateStatEntity staticTemplate(EsTemplateEntity templateEntity) {
        long end1 = System.currentTimeMillis();
        log.info("时间 模板 周期：{}", Long.valueOf(end1));
        String indexPatterns = templateEntity.getIndexPatterns();
        List<String> indexPatternList = elasticRealTemplateService.getIndexPatternList(indexPatterns);
        if (indexPatternList.isEmpty()) {
            return null;
        }
        long end2 = System.currentTimeMillis();
        log.info("时间 模板 周期：{}", Long.valueOf(end2 - end1));

        Set<EsLifeCycleManagement> lifeExSet = new HashSet<>();
        Map<String, IndexEntity> indexAllMap = new HashMap<>();
        Set<ShardEntity> listAllShard = new HashSet<>();
        Set<SegmentMessage> listAllSegment = new HashSet<>();

        for (String indexPatternPrefix : indexPatternList) {
            String indexXing = indexPatternPrefix + "*";
            List<EsLifeCycleManagement> lifeErrors = null;
            try {
                lifeErrors = this.lifecyclePoliciesService.getLifeCycleExList(indexXing);
            } catch (IOException e) {
                log.error("获取生命周期异常", e);
            }
            for (EsLifeCycleManagement item : lifeErrors) {
                String index = item.getIndex();
                if (index.startsWith(indexPatternPrefix)) {
                    lifeExSet.add(item);
                }
            }
            long end3 = System.currentTimeMillis();
            log.info("时间 模板 周期问题：{}", Long.valueOf(end3 - end2));

            List<IndexEntity> indexList = null;
            try {
                indexList = this.elasticRealIndexService.listIndexNameByPrefix(indexPatternPrefix, indexXing);
            } catch (IOException e) {
                log.error("获取索引列表失败", e);
            }
            Map<String, IndexEntity> indexMap =   indexList.stream().collect(Collectors.toMap(IndexEntity::getIndex, Function.identity()));
            indexAllMap.putAll(indexMap);

            long end4 = System.currentTimeMillis();
            log.info("时间 模板 索引：{}", Long.valueOf(end4 - end3));

            List<ShardEntity> listShard = null;
            try {
                listShard = this.elasticShardService.listShardByPrefix(indexPatternPrefix, indexXing);
            } catch (IOException e) {
                log.error("获取shard失败：{}", e.getMessage());
            }
            listAllShard.addAll(listShard);

            long end5 = System.currentTimeMillis();
            log.info("时间 模板 shard：{}", Long.valueOf(end5 - end4));

            List<SegmentMessage> listSegment = null;
            try {
                listSegment = this.elasticSegmentService.listSegmentByPrefix(indexPatternPrefix, indexXing);
            } catch (IOException e) {
                log.error("获取segment失败", e);
            }
            listAllSegment.addAll(listSegment);

            long l1 = System.currentTimeMillis();
            log.info("时间 模板 segment：{}", Long.valueOf(l1 - end5));
        }

        long end6 = System.currentTimeMillis();



        int openIndex = 0;
        int closeIndex = 0;
        int exceptionIndex = 0;
        int freezeIndex = 0;

        int shardUnassigned = 0;

        long segmentSize = 0L;

        for (Map.Entry<String, IndexEntity> mapItem : indexAllMap.entrySet()) {
            IndexEntity indexEntity = mapItem.getValue();

            for (ShardEntity shard : listAllShard) {
                String shardState = shard.getState();
                if ("UNASSIGNED".equalsIgnoreCase(shardState)) {
                    shardUnassigned++;
                }
            }

            for (SegmentMessage segmentItem : listAllSegment) {
                Long sizeMemory = segmentItem.getSizeMemory();
                segmentSize += sizeMemory.longValue();
            }

            String status = indexEntity.getStatus();
            if (status.equals("open")) {
                openIndex++;
            }
            if (status.equals("close")) {
                closeIndex++;
            }

            String health = indexEntity.getHealth();
            if ("red".equalsIgnoreCase(health) || "yellow".equalsIgnoreCase(health)) {
                exceptionIndex++;
            }

            String settings = indexEntity.getSettings();

            if (settings != null && settings.contains("\"frozen\":\"true\"")) {
                freezeIndex++;
            }
        }

        EsTemplateStatEntity et = new EsTemplateStatEntity();
        et.setId(templateEntity.getId());
        et.setRollingCycleError(Integer.valueOf(lifeExSet.size()));
        et.setOpenIndex(Integer.valueOf(openIndex));
        et.setCloseIndex(Integer.valueOf(closeIndex));
        et.setExceptionIndex(Integer.valueOf(exceptionIndex));
        et.setFreezeIndex(Integer.valueOf(freezeIndex));

        et.setShardCount(Integer.valueOf(listAllShard.size()));
        et.setShardUnassigned(Integer.valueOf(shardUnassigned));

        et.setSegmetCount(Integer.valueOf(listAllSegment.size()));
        et.setSegmentSize(Long.valueOf(segmentSize / 1048576L));

        long end7 = System.currentTimeMillis();
        log.info("时间 模板 遍历：{}", Long.valueOf(end7 - end6));

        return et;
    }


    /**
     * 从索引模式中提取索引前缀
     *
     * @param indexPattern 索引模式，例如 lcc-log-YYYYMMDD 或者 lcc-log-YYYYMMDDHH
     * @return 索引前缀，例如 lcc-log
     */
    private String getIndexPrefix(String indexPattern) {
        // 假设索引模式至少包含一个日期占位符如 "YYYY", "MM", "DD", "HH" 等等
        // 使用正则表达式匹配最后一个非数字字母下划线字符的位置
        int pos = indexPattern.replaceAll("[^\\d_]*([\\d_]+)$", "$1").length();

        // 提取前缀
        String indexPrefix = indexPattern.substring(0, indexPattern.length() - pos);

        return indexPrefix;
    }

}
