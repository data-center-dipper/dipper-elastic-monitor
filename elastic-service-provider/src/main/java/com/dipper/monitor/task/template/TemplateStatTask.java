package com.dipper.monitor.task.template;

import com.alibaba.fastjson.JSON;
import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.utils.elastic.EsDateUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TemplateStatTask {

    @Autowired
    private ElasticStoreTemplateService elasticStoreTemplateService;
    @Autowired
    private ElasticRealIndexService elasticRealIndexService;

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
            // 根据索引获取索引的详细信息并进行统计
            Map<String, Integer> statusCount = indices.stream()
                    .collect(Collectors.groupingBy(IndexEntity::getStatus, Collectors.summingInt(e -> 1)));
            log.info("模板 {} 下的索引状态统计: {}", templateEntity.getName(), JSON.toJSONString(statusCount));
        } catch (Exception e) {
            log.error("处理模板 {} 的索引时发生错误", templateEntity.getName(), e);
        }
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
