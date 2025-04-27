package com.dipper.monitor.task.template;

import com.alibaba.fastjson.JSON;
import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.entity.elastic.life.EsLifeCycleManagement;
import com.dipper.monitor.entity.elastic.life.EsTemplateConfigMes;
import com.dipper.monitor.entity.elastic.segments.SegmentMessage;
import com.dipper.monitor.entity.elastic.shard.ShardEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.life.LifecyclePoliciesService;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.utils.elastic.EsDateUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        for (EsTemplateEntity templateEntity : allTemplates) {
            staticTemplate(templateEntity);
        }
    }

    /**
     * 计算一个模板下有多少个索引，开启的有多少个 关闭的有多少个 冻结的有多少个 异常的有多少个
     * 分片数有多少个，段有多少个
     * 占用的总jmv信息有多少
     *
     * 索引：共 35个，关闭 4个 ，冻结 - ，开启 31个
     * 滚动周期：日
     * 异常索引个数：-，分片个数：268个 ，异常分片个数：-
     * 段：共 399个 ，JVM使用： 5MB
     *
     * 这些统计信息
     *
     * @param templateEntity 模板实体
     */
    private void staticTemplate(EsTemplateEntity templateEntity) {
        String indexPatterns = templateEntity.getIndexPatterns();
        // 获取索引前缀
        String indexPrefix = getIndexPrefix(indexPatterns);
        String indexXing = indexPrefix + "*";
        try {
            List<IndexEntity> indices = elasticRealIndexService.listIndexNameByPrefix(indexPrefix, indexXing);
            if (indices == null || indices.isEmpty()) {
                log.warn("没有找到与模板 {} 匹配的索引", templateEntity.getEnName());
                return;
            }

        } catch (Exception e) {
            log.error("处理模板 {} 的索引时发生错误", templateEntity.getEnName(), e);
        }
    }

    public EsTemplateConfigMes listStatisticalByOneTemplate(EsTemplateEntity templateEntity) throws IOException {
        long end1 = System.currentTimeMillis();
        log.info("时间 模板 周期：{}", Long.valueOf(end1));
        String indexPatterns = templateEntity.getIndexPatterns();
        List<String> indexPatternList = elasticRealTemplateService.getIndexPatternList(indexPatterns);
        if (indexPatternList.isEmpty()) {
            return new EsTemplateConfigMes();
        }
        long end2 = System.currentTimeMillis();
        log.info("时间 模板 周期：{}", Long.valueOf(end2 - end1));

        Set<EsLifeCycleManagement> lifeExSet = new HashSet<>();
        Map<String, IndexEntity> indexAllMap = new HashMap<>();
        Set<ShardEntity> listAllShard = new HashSet<>();
        Set<SegmentMessage> listAllSegment = new HashSet<>();

        for (String indexPatternPrefix : indexPatternList) {
            String indexXing = indexPatternPrefix + "*";
            List<EsLifeCycleManagement> lifeErrors = this.lifecyclePoliciesService.getLifeCycleExList(indexXing);
            for (EsLifeCycleManagement item : lifeErrors) {
                String index = item.getIndex();
                if (index.startsWith(indexPatternPrefix)) {
                    lifeExSet.add(item);
                }
            }
            long end3 = System.currentTimeMillis();
            log.info("时间 模板 周期问题：{}", Long.valueOf(end3 - end2));

            Map<String, IndexEntity> indexMap = this.elasticRealIndexService.listIndexPatternMapThread(true, indexPatternPrefix, indexXing);
            indexAllMap.putAll(indexMap);

            long end4 = System.currentTimeMillis();
            log.info("时间 模板 索引：{}", Long.valueOf(end4 - end3));

            List<Shard> listShard = this.shardService.listShardByPrefix(indexPatternPrefix, indexXing);
            listAllShard.addAll(listShard);

            long end5 = System.currentTimeMillis();
            log.info("时间 模板 shard：{}", Long.valueOf(end5 - end4));

            List<SegmentMessage> listSegment = this.esSegmentService.listSegmentByPrefix(indexPatternPrefix, indexXing);
            listAllSegment.addAll(listSegment);

            long l1 = System.currentTimeMillis();
            log.info("时间 模板 segment：{}", Long.valueOf(l1 - end5));
        }

        long end6 = System.currentTimeMillis();

        EsTemplateConfigMes et = new EsTemplateConfigMes();

        conf.setTemplateConfigNameValue("");
        BeanUtils.copyProperties(conf, et);

        int openIndex = 0;
        int closeIndex = 0;
        int exceptionIndex = 0;
        int freezeIndex = 0;

        int shardUnassigned = 0;

        long segmentSize = 0L;

        for (Map.Entry<String, IndexEntity> mapItem : indexAllMap.entrySet()) {
            IndexEntity indexEntity = mapItem.getValue();

            for (Shard shard : listAllShard) {
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

            String settings = indexEntity.getSetting();

            if (settings != null && settings.contains("\"frozen\":\"true\"")) {
                freezeIndex++;
            }
        }

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
     * @param indexPatterns 索引模式，例如 lcc-log-YYYYMMDD 或者 lcc-log-YYYYMMDDHH
     * @return 索引前缀，例如 lcc-log
     */
    private String getIndexPrefix(String indexPatterns) {
            String indexParttonFromWeb = null;
            int nowDateTime = EsDateUtils.getNowDateInt(format);
            if ("yyyy".equals(format)) {
                indexParttonFromWeb = prefix + prefix;
            } else if ("yyyyMM".equals(format)) {
                indexParttonFromWeb = prefix + prefix;
            } else if ("yyyyMMdd".equals(format)) {
                indexParttonFromWeb = prefix + prefix;
            }
            return indexParttonFromWeb;
    }

}
