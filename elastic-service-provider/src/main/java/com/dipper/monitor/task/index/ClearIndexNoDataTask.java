package com.dipper.monitor.task.index;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.comment.QuartzManager;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.index.IndexOneOperatorService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.task.AbstractITask;
import com.dipper.monitor.task.ITask;
import com.dipper.monitor.utils.elastic.FeatureIndexUtils;
import com.dipper.monitor.utils.elastic.IndexPatternsUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class ClearIndexNoDataTask extends AbstractITask  {

    @Autowired
    protected ElasticRealIndexService elasticRealIndexService;
    @Autowired
    protected ElasticStoreTemplateService elasticStoreTemplateService;
    @Autowired
    protected IndexOneOperatorService indexOneOperatorService;

    // 支持的滚动类型
    private static final String PATTERN_YYYYMMDD = "yyyyMMdd";
    private static final String PATTERN_YYYY_MM_DD = "yyyy.MM.dd";
    private static final String PATTERN_YYYYMM = "yyyyMM";
    private static final String PATTERN_YYYY_MM = "yyyy.MM";
    private static final String PATTERN_YYYY = "yyyy";

    public static final String[] DAILY_PATTERNS = {PATTERN_YYYYMMDD, PATTERN_YYYY_MM_DD};
    public static final String[] MONTHLY_PATTERNS = {PATTERN_YYYYMM, PATTERN_YYYY_MM};
    public static final String[] YEARLY_PATTERNS = {PATTERN_YYYY};

    private AtomicLong numRun = new AtomicLong(0);

    @Autowired
    private QuartzManager quartzManager;

    public void clearIndexNoData() {
        List<EsTemplateEntity> allTemplates = elasticStoreTemplateService.getAllTemplates();
        if (allTemplates == null || allTemplates.isEmpty()) return;

        for (EsTemplateEntity item : allTemplates) {
            String indexPatterns = item.getIndexPatterns();
            List<IndexEntity> indexEntities = null;
            try {
                indexEntities = elasticRealIndexService.listIndexNameByIndexPatterns(indexPatterns);
            } catch (Exception e) {
                log.error("listIndexNameByIndexPatterns error, indexPatterns: {}", indexPatterns, e);
            }
            if (indexEntities == null || indexEntities.isEmpty()) continue;

            for (IndexEntity indexEntity : indexEntities) {
                String index = indexEntity.getIndex();

                // 排除 feature index
                if (FeatureIndexUtils.isFeatureIndex(index)) {
                    continue;
                }

                // 提取索引中的日期信息
                String dateStr = IndexPatternsUtils.extractDateFromIndexName(index, indexPatterns);
                if (dateStr == null) {
                    log.warn("无法解析索引 {} 的日期信息", index);
                    continue;
                }

                LocalDate indexDate = null;
                try {
                    indexDate = LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    log.warn("解析日期失败: {}", dateStr);
                    continue;
                }

                // 判断是否为未来索引
                if (indexDate.isAfter(LocalDate.now())) {
                    log.info("跳过未来索引: {}", index);
                    continue;
                }

                // 判断滚动类型并检查是否在保留期内
                if (isPatternMatch(item, DAILY_PATTERNS) && !isOlderThanDays(indexDate, 7)) {
                    log.info("索引 {} 属于每日滚动，最近7天内，跳过删除", index);
                    continue;
                }

                if (isPatternMatch(item, MONTHLY_PATTERNS) && !isOlderThanMonths(indexDate, 2)) {
                    log.info("索引 {} 属于每月滚动，最近2个月内，跳过删除", index);
                    continue;
                }

                if (isPatternMatch(item, YEARLY_PATTERNS) && !isOlderThanYears(indexDate, 1)) {
                    log.info("索引 {} 属于每年滚动，最近1年内，跳过删除", index);
                    continue;
                }

                // 检查是否有数据
                Long docsCount = indexEntity.getDocsCount();
                if (docsCount != null && docsCount > 0) {
                    log.info("索引 {} 包含 {} 条数据，跳过删除", index, docsCount);
                    continue;
                }

                // 执行删除
                try {
                    indexOneOperatorService.deleteIndex(index);
                    log.info("成功删除无数据索引: {}", index);
                } catch (Exception e) {
                    log.error("删除索引失败: {}", index, e);
                }
            }
        }
    }

    // 判断是否匹配某类 pattern
    private boolean isPatternMatch(EsTemplateEntity item, String[] patterns) {
        String pattern = item.getIndexPatterns();
        for (String p : patterns) {
            if (pattern.contains(p)) {
                return true;
            }
        }
        return false;
    }

    // 是否超过 N 天前
    private boolean isOlderThanDays(LocalDate date, int days) {
        return date.isBefore(LocalDate.now().minusDays(days));
    }

    // 是否超过 N 个月前
    private boolean isOlderThanMonths(LocalDate date, int months) {
        return date.isBefore(LocalDate.now().minusMonths(months));
    }

    // 是否超过 N 年前
    private boolean isOlderThanYears(LocalDate date, int years) {
        return date.isBefore(LocalDate.now().minusYears(years));
    }

    @Override
    public String getCron() {
        return "0 0/1 * * * ?";
    }

    @Override
    public void setCron(String cron) {

    }

    @Override
    public String getAuthor() {
        return "lcc";
    }

    @Override
    public String getJobDesc() {
        return "清空没有数据的索引";
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void execute() {
        long andIncrement = numRun.getAndIncrement();
        clearIndexNoData();
    }

    @Override
    public String getTaskName() {
        return "清空没有数据的索引";
    }
}