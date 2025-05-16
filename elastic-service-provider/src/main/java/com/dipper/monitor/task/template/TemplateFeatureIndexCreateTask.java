package com.dipper.monitor.task.template;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.alians.ElasticAliansService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.life.ElasticRealLifecyclePoliciesService;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.service.elastic.template.TemplatePreviewService;
import com.dipper.monitor.service.elastic.template.impl.handlers.preview.PreviewCanRunTemplateHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.rolling.feature.DaysOfFeatureIndexHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class TemplateFeatureIndexCreateTask {

    private static final Logger log = LoggerFactory.getLogger(TemplateFeatureIndexCreateTask.class);

    @Autowired
    protected ElasticStoreTemplateService elasticStoreTemplateService;
    @Autowired
    protected ElasticClientService elasticClientService;
    @Autowired
    protected TemplatePreviewService templatePreviewService;
    @Autowired
    protected ElasticRealTemplateService elasticRealTemplateService;
    @Autowired
    protected ElasticAliansService elasticAliansService;
    @Autowired
    protected ElasticRealLifecyclePoliciesService elasticRealLifecyclePoliciesService;
    @Autowired
    protected ElasticRealIndexService elasticRealIndexService;


    // 改成每小时执行一次
    @QuartzJob(cron = "0 0/10 * * * ?",
            author = "hydra",
            groupName = "hydra",
            jobDesc = "elastic模板信息统计",
            editAble = true)
    public void elasticCreateFeatureIndexTask() throws Exception {
        // 1. 获取所有模版信息
        List<EsTemplateEntity> allTemplates = elasticStoreTemplateService.getAllTemplates();
        if (allTemplates == null || allTemplates.isEmpty()) {
            log.info("没有找到模板信息，跳过创建未来索引");
            return;
        }
        
        // 2. 遍历所有模版信息，按照不同的时间格式处理
        List<EsUnconvertedTemplate> yearTemplates = new ArrayList<>();
        List<EsUnconvertedTemplate> halfYearTemplates = new ArrayList<>();
        List<EsUnconvertedTemplate> monthTemplates = new ArrayList<>();
        List<EsUnconvertedTemplate> dayTemplates = new ArrayList<>();
        
        for (EsTemplateEntity template : allTemplates) {
            EsUnconvertedTemplate unconvertedTemplate = elasticStoreTemplateService.getOneUnconvertedTemplate(template.getId());
            if (unconvertedTemplate == null) {
                continue;
            }
            
            String indexPatterns = unconvertedTemplate.getIndexPatterns();
            if (indexPatterns == null || indexPatterns.isEmpty()) {
                continue;
            }
            
            // 3. 判断索引是否包含 yyyy，不包含说明不是按照时间滚动的，跳过
            if (!indexPatterns.contains("yyyy")) {
                continue;
            }
            
            // 4. 根据不同的时间格式分类
            if (indexPatterns.contains("yyyyMMdd")) {
                dayTemplates.add(unconvertedTemplate);
            } else if (indexPatterns.contains("yyyyMM")) {
                // 判断是否是半年滚动模板
                if (unconvertedTemplate.getEnName() != null && 
                    unconvertedTemplate.getEnName().contains("half_year")) {
                    halfYearTemplates.add(unconvertedTemplate);
                } else {
                    monthTemplates.add(unconvertedTemplate);
                }
            } else if (indexPatterns.contains("yyyy")) {
                yearTemplates.add(unconvertedTemplate);
            }
        }
        
        // 5. 根据不同的时间格式创建未来索引
        createDaysFeatureIndex(dayTemplates);
        createMonthFeatureIndex(monthTemplates);
        createHafYearFeatureIndex(halfYearTemplates);
        createYearFeatureIndex(yearTemplates);
    }

    /**
     * 创建未来索引 索引模版是 lcc-log-yyyy-* 那么当前是 2025 那么当日索引是
     * lcc-log-2025-000001 未来是
     * lcc-log-2026-000001等
     * 如果未来索引已经存在那么跳过
     */
    private void createYearFeatureIndex(List<EsUnconvertedTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            return;
        }
        
        log.info("开始创建按年滚动的未来索引，模板数量: {}", templates.size());
        
        // 创建未来1年的索引
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int nextYear = currentYear + 1;
        
        for (EsUnconvertedTemplate template : templates) {
            try {
                // 检查未来年份的索引是否已存在
                String indexPattern = template.getIndexPatterns();
                String nextYearPattern = indexPattern.replace("yyyy", String.valueOf(nextYear));
                List<String> existingIndices = elasticRealIndexService.listIndexNameByPrefix(nextYearPattern);
                
                if (existingIndices != null && !existingIndices.isEmpty()) {
                    log.info("未来年份 {} 的索引已存在，跳过创建", nextYear);
                    continue;
                }
                
                // 创建未来年份的索引
                String nextYearStr = String.valueOf(nextYear);
                log.info("为模板 {} 创建未来年份 {} 的索引", template.getEnName(), nextYearStr);
                
                // 生成模板并创建索引
                JSONObject templateJson = templatePreviewService.previewEffectTemplate(template.getId());
                createIndexWithDate(template, templateJson, nextYearStr, "yyyy");
                
            } catch (Exception e) {
                log.error("创建年度索引时发生错误: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 创建未来索引 索引模版是 lcc-log-yyyyMM-* 那么当前是 202505 那么当日索引是
     * lcc-log-202505-000001 未来是
     * lcc-log-202506-000001等
     * 如果未来索引已经存在那么跳过
     */
    private void createMonthFeatureIndex(List<EsUnconvertedTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            return;
        }
        
        log.info("开始创建按月滚动的未来索引，模板数量: {}", templates.size());
        
        // 创建未来3个月的索引
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        
        for (EsUnconvertedTemplate template : templates) {
            try {
                for (int i = 1; i <= 3; i++) {
                    // 计算未来月份
                    Calendar futureCalendar = (Calendar) calendar.clone();
                    futureCalendar.add(Calendar.MONTH, i);
                    String futureMonth = sdf.format(futureCalendar.getTime());
                    
                    // 检查未来月份的索引是否已存在
                    String indexPattern = template.getIndexPatterns();
                    String futureMonthPattern = indexPattern.replace("yyyyMM", futureMonth);
                    List<String> existingIndices = elasticRealIndexService.listIndexNameByPrefix(futureMonthPattern);
                    
                    if (existingIndices != null && !existingIndices.isEmpty()) {
                        log.info("未来月份 {} 的索引已存在，跳过创建", futureMonth);
                        continue;
                    }
                    
                    // 创建未来月份的索引
                    log.info("为模板 {} 创建未来月份 {} 的索引", template.getEnName(), futureMonth);
                    
                    // 生成模板并创建索引
                    JSONObject templateJson = templatePreviewService.previewEffectTemplate(template.getId());
                    createIndexWithDate(template, templateJson, futureMonth, "yyyyMM");
                }
            } catch (Exception e) {
                log.error("创建月度索引时发生错误: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 创建未来半年索引 索引模版是 lcc-log-yyyyMM-* 
     * 上半年(1-6月)使用1月: lcc-log-202501-000001
     * 下半年(7-12月)使用7月: lcc-log-202507-000001
     * 如果未来索引已经存在那么跳过
     */
    private void createHafYearFeatureIndex(List<EsUnconvertedTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            return;
        }
        
        log.info("开始创建按半年滚动的未来索引，模板数量: {}", templates.size());
        
        // 获取当前日期和下一个半年日期
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH 从0开始
        
        // 确定当前半年和下一个半年
        String nextHalfYear;
        if (currentMonth <= 6) {
            // 当前是上半年，下一个是当年下半年
            nextHalfYear = String.format("%d07", currentYear);
        } else {
            // 当前是下半年，下一个是明年上半年
            nextHalfYear = String.format("%d01", currentYear + 1);
        }
        
        for (EsUnconvertedTemplate template : templates) {
            try {
                // 检查未来半年的索引是否已存在
                String indexPattern = template.getIndexPatterns();
                String nextHalfYearPattern = indexPattern.replace("yyyyMM", nextHalfYear);
                List<String> existingIndices = elasticRealIndexService.listIndexNameByPrefix(nextHalfYearPattern);
                
                if (existingIndices != null && !existingIndices.isEmpty()) {
                    log.info("未来半年 {} 的索引已存在，跳过创建", nextHalfYear);
                    continue;
                }
                
                // 创建未来半年的索引
                log.info("为模板 {} 创建未来半年 {} 的索引", template.getEnName(), nextHalfYear);
                
                // 生成模板并创建索引
                JSONObject templateJson = templatePreviewService.previewEffectTemplate(template.getId());
                createIndexWithDate(template, templateJson, nextHalfYear, "yyyyMM");
                
            } catch (Exception e) {
                log.error("创建半年索引时发生错误: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 创建未来索引 索引模版是 lcc-log-yyyyMMdd-* 那么当前是 20250516 那么当日索引是
     * lcc-log-20250516-000001 未来是
     * lcc-log-20250517-000001 lcc-log-20250518-000001 lcc-log-20250519-000001 等
     * 如果未来索引已经存在那么跳过
     */
    private void createDaysFeatureIndex(List<EsUnconvertedTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            return;
        }
        
        log.info("开始创建按天滚动的未来索引，模板数量: {}", templates.size());

        for (EsUnconvertedTemplate template : templates) {
            try {
                DaysOfFeatureIndexHandler daysOfFeatureIndexHandler = new DaysOfFeatureIndexHandler(template);
                daysOfFeatureIndexHandler.handle();
            } catch (Exception e) {
                log.error("创建日期索引时发生错误: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * 使用指定日期创建索引
     * @param template 模板信息
     * @param templateJson 模板JSON
     * @param dateStr 日期字符串
     * @param dateFormat 日期格式 (yyyy, yyyyMM, yyyyMMdd)
     */
    private void createIndexWithDate(EsUnconvertedTemplate template, JSONObject templateJson, String dateStr, String dateFormat) {
        try {
            // 1. 获取索引模式
            String indexPatterns = template.getIndexPatterns();
            
            // 2. 替换日期部分
            indexPatterns = indexPatterns.replace(dateFormat, dateStr);
            if (indexPatterns.endsWith("-*")) {
                indexPatterns = indexPatterns.substring(0, indexPatterns.length() - 2);
            } else if (indexPatterns.endsWith("*")) {
                indexPatterns = indexPatterns.substring(0, indexPatterns.length() - 1);
            }
            
            // 3. 提取前缀
            String prefix = indexPatterns;
            
            // 4. 生成索引名称，格式为：prefix-0000001
            String indexName = String.format("%s-%07d", prefix, 1);
            log.info("生成索引名称: {}", indexName);
            
            // 5. 生成别名，格式为：prefix
            String aliasName = prefix;
            log.info("生成别名: {}", aliasName);
            
            // 6. 创建索引
            elasticClientService.createIndex(indexName, templateJson);
            
            // 7. 添加别名
            elasticAliansService.addAlias(indexName, aliasName);
            
            log.info("成功创建未来索引: {}, 别名: {}", indexName, aliasName);
        } catch (Exception e) {
            log.error("创建索引时发生错误: {}", e.getMessage(), e);
        }
    }
}
