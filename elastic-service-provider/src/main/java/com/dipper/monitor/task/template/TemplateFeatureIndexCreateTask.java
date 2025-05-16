package com.dipper.monitor.task.template;

import com.dipper.monitor.annotation.quartz.QuartzJob;
import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.alians.ElasticAliansService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.life.ElasticRealLifecyclePoliciesService;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import com.dipper.monitor.service.elastic.template.TemplatePreviewService;
import com.dipper.monitor.service.elastic.template.impl.handlers.rolling.feature.DaysOfFeatureIndexHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.rolling.feature.HafYearFeatureIndexHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.rolling.feature.MonthOfFeatureIndexHandler;
import com.dipper.monitor.service.elastic.template.impl.handlers.rolling.feature.YearFeatureIndexHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

        log.info("开始创建按天滚动的未来索引，模板数量: {}", templates.size());

        for (EsUnconvertedTemplate template : templates) {
            try {
                YearFeatureIndexHandler yearFeatureIndexHandler = new YearFeatureIndexHandler(template);
                yearFeatureIndexHandler.handle();
            } catch (Exception e) {
                log.error("创建日期索引时发生错误: {}", e.getMessage(), e);
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

        log.info("开始创建按天滚动的未来索引，模板数量: {}", templates.size());

        for (EsUnconvertedTemplate template : templates) {
            try {
                MonthOfFeatureIndexHandler monthOfFeatureIndexHandler = new MonthOfFeatureIndexHandler(template);
                monthOfFeatureIndexHandler.handle();
            } catch (Exception e) {
                log.error("创建日期索引时发生错误: {}", e.getMessage(), e);
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

        log.info("开始创建按天滚动的未来索引，模板数量: {}", templates.size());

        for (EsUnconvertedTemplate template : templates) {
            try {
                HafYearFeatureIndexHandler hafYearFeatureIndexHandler = new HafYearFeatureIndexHandler(template);
                hafYearFeatureIndexHandler.handle();
            } catch (Exception e) {
                log.error("创建日期索引时发生错误: {}", e.getMessage(), e);
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
                processOneTemplate(template);
            } catch (Exception e) {
                log.error("创建日期索引时发生错误: {}", e.getMessage(), e);
            }
        }
    }

    private void processOneTemplate(EsUnconvertedTemplate template) throws IOException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        for (int i = 1; i <= 3; i++) {
            Calendar futureCalendar = (Calendar) calendar.clone();
            futureCalendar.add(Calendar.DAY_OF_MONTH, i);
            String futureDate = sdf.format(futureCalendar.getTime());

            DaysOfFeatureIndexHandler daysOfFeatureIndexHandler = new DaysOfFeatureIndexHandler(template,futureDate);
            daysOfFeatureIndexHandler.handle();
        }
    }

}
