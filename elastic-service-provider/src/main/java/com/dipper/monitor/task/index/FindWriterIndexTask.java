package com.dipper.monitor.task.index;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.db.elastic.IndexWriteEntity;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.index.IndexWriteService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.utils.elastic.IndexPatternsUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class FindWriterIndexTask {

    private static final Logger log = LoggerFactory.getLogger(FindWriterIndexTask.class);

    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;

    @Autowired
    private ElasticClientService elasticClientService;

    @Autowired
    private ElasticRealIndexService elasticRealIndexService;

    @Autowired
    private IndexWriteService indexWriteService;

    private List<IndexWriteEntity> indexWriteEntities = new ArrayList<>();

    // 每小时执行一次
    @QuartzJob(cron = "0 0 * * * ?",
            author = "hydra",
            groupName = "elastic_monitor",
            jobDesc = "统计Elasticsearch中正在写入的索引信息",
            editAble = true)
    public void findWriterIndexTask() {
        try {
            List<EsTemplateEntity> allTemplates = elasticStoreTemplateService.getAllTemplates();
            for (EsTemplateEntity template : allTemplates) {
                try {
                    processOneTemplate(template);
                }catch (Exception e){
                    log.error("Error while scanning writer indexes", e);
                }
            }
            if(!indexWriteEntities.isEmpty()){
                indexWriteService.deleteAll();
                indexWriteService.saveBatch(indexWriteEntities);
            }
        } catch (Exception e) {
            log.error("Error while scanning writer indexes", e);
        }finally {
            indexWriteEntities.clear();
        }
    }

    private void processOneTemplate(EsTemplateEntity template) {
        String indexPatterns = template.getIndexPatterns();
        String datePattern = IndexPatternsUtils.getIndexDatePattern(indexPatterns);
        String indexPrefixNoDate = IndexPatternsUtils.getIndexPrefixNoDate(indexPatterns);

        if (StringUtils.isBlank(datePattern)) {
            processNoDateTemplate(template);
            return;
        }

        switch (datePattern) {
            case "yyyyMMdd":
                processDayTemplate(template, indexPrefixNoDate);
                break;
            case "yyyyMM":
                processMonthTemplate(template, indexPrefixNoDate);
                break;
            case "yyyy":
                processYearTemplate(template, indexPrefixNoDate);
                break;
            default:
                log.warn("Unsupported date pattern: {}", datePattern);
        }
    }

    private void processDayTemplate(EsTemplateEntity template, String indexPrefixNoDate) {
        List<String> dateList = IndexPatternsUtils.getLastNDays(7, "yyyyMMdd");
        scanAndProcessIndexes(template, indexPrefixNoDate, dateList);
    }

    private void processMonthTemplate(EsTemplateEntity template, String indexPrefixNoDate) {
        List<String> dateList = IndexPatternsUtils.getLastNMonths(3, "yyyyMM");
        scanAndProcessIndexes(template, indexPrefixNoDate, dateList);
    }

    private void processYearTemplate(EsTemplateEntity template, String indexPrefixNoDate) {
        List<String> dateList = IndexPatternsUtils.getLastNYears(2, "yyyy");
        scanAndProcessIndexes(template, indexPrefixNoDate, dateList);
    }

    private void processNoDateTemplate(EsTemplateEntity template) {
        String indexPatterns = template.getIndexPatterns();
        List<IndexEntity> indexList = null;
        try {
            indexList = elasticRealIndexService.listIndexNameByIndexPatterns(indexPatterns);
        } catch (Exception e) {
            log.error("获取索引列表失败", e);
        }
        List<IndexWriteEntity> indexWriteEntities = processIndexes(template, indexList);
        if(CollectionUtils.isNotEmpty(indexWriteEntities)){
            indexWriteEntities.addAll(indexWriteEntities);
        }
    }

    private void scanAndProcessIndexes(EsTemplateEntity template, String indexPrefixNoDate, List<String> dateList) {
        for (String date : dateList) {
            String indexXing = indexPrefixNoDate + date + "*";
            List<IndexEntity> indexList = null;
            try {
                indexList = elasticRealIndexService.listIndexNameByPrefix(indexPrefixNoDate, indexXing);
            } catch (IOException e) {
                log.error("获取索引列表失败", e);
            }
            List<IndexWriteEntity> indexWriteEntities = processIndexes(template, indexList);
            if(CollectionUtils.isNotEmpty(indexWriteEntities)){
                indexWriteEntities.addAll(indexWriteEntities);
            }
        }
    }


    private List<Long> getFiveCount(String indexName) {
        try {
            List<Long> result = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Long docCount = elasticRealIndexService.getDocumentCount(indexName);
                if (docCount == null) {
                    log.warn("Failed to get document count for index: {}", indexName);
                    return Collections.emptyList();
                }
                result.add(docCount);
                TimeUnit.MINUTES.sleep(1);
            }
            return result;
        } catch (Exception e) {
            log.error("Error getting document count for index: {}", indexName, e);
            return Collections.emptyList();
        }
    }

    private  List<IndexWriteEntity> processIndexes(EsTemplateEntity template, List<IndexEntity> indexList) {
        if (indexList == null || indexList.isEmpty()) return Collections.emptyList();

        // 按文档数排序，取前3个最大索引
        indexList.sort((a, b) -> b.getDocsCount().compareTo(a.getDocsCount()));
        List<IndexEntity> latestIndexes = indexList.subList(0, Math.min(3, indexList.size()));

        List<IndexWriteEntity> result = new ArrayList<>();
        for (IndexEntity indexEntity : latestIndexes) {
            String indexName = indexEntity.getIndex();

            // 获取 5 次文档数
            List<Long> counts = getFiveCount(indexName);
            if (counts.isEmpty() || counts.size() < 2) {
                log.warn("Not enough data to determine write status for index: {}", indexName);
                continue;
            }

            // 判断是否正在写入
            boolean isWriting = isIndexInWriting(counts);
            if (!isWriting) {
                log.info("Index {} is not being written to.", indexName);
                continue;
            }

            // 获取 mapping 并统计字段数
            JSONObject mappingJson = elasticRealIndexService.getMappingByIndexName(indexName);
            int fieldCount = getFieldCount(mappingJson);

            // 特殊字符检查
            boolean hasSpecialChar = hasSpecialChar(indexName);

            // 计算写入速率（每秒平均）
            double writeRate = calculateWriteRate(counts);

            // 存入数据库
            IndexWriteEntity indexWriteEntity = transToEntity(template, indexEntity, fieldCount, hasSpecialChar, writeRate);
            result.add(indexWriteEntity);
        }
        return result;
    }

    /**
     * 判断是否正在写入：只要有一个点比前一个大就认为在写入
     */
    private boolean isIndexInWriting(List<Long> counts) {
        for (int i = 1; i < counts.size(); i++) {
            if (counts.get(i) > counts.get(i - 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据5次文档数计算平均写入速率（单位：条/秒）
     */
    private double calculateWriteRate(List<Long> counts) {
        long first = counts.get(0);
        long last = counts.get(counts.size() - 1);
        long delta = last - first;
        int seconds = (counts.size() - 1) * 60; // 每次间隔1分钟（60秒）

        if (seconds <= 0) return 0.0;
        return (double) delta / seconds;
    }

    private int getFieldCount(JSONObject mappingJson) {
        JSONObject properties = mappingJson.getJSONObject("mappings").getJSONObject("properties");
        return properties != null ? properties.keySet().size() : 0;
    }

    private boolean hasSpecialChar(String indexName) {
        return !indexName.matches("^[a-zA-Z0-9\\-_\\.]*$");
    }

    private IndexWriteEntity transToEntity(EsTemplateEntity template, IndexEntity indexEntity,
                               int fieldCount, boolean hasSpecialChar, double writeRate) {
        IndexWriteEntity stat = new IndexWriteEntity();
        stat.setTemplateName(template.getEnName());
        stat.setIndexName(indexEntity.getIndex());
        stat.setAlias(indexEntity.getAlians());
        stat.setCanWrite(indexEntity.getIndexCanWrite());
        stat.setFieldCount(fieldCount);
        stat.setHasSpecialChar(hasSpecialChar);
        stat.setWriteRate(writeRate);
        stat.setDocCount(indexEntity.getDocsCount());
        stat.setCreateTime(new Date());
        return stat;
    }
}