package com.dipper.monitor.service.elastic.template.impl.handlers.history;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.TemplatePageInfo;
import com.dipper.monitor.entity.elastic.template.history.EsTemplateInfo;
import com.dipper.monitor.entity.elastic.template.history.TemplateHistoryView;
import com.dipper.monitor.utils.ListUtils;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 获取当前使用模板分页信息
 * 如果模版是按年分的，那么就是当年的模版
 * 如果模版是按月分的，那么就是当月的模版
 * 如果模版是按天分的，那么就是当天的模版
 */
@Slf4j
@Component
public class CurrentHistoryTemplateHandler extends AbstractHistoryTemplateHandler {

    // 年份模式，如 logstash-2023
    private static final Pattern YEAR_PATTERN = Pattern.compile(".*-(\\d{4}).*");
    // 月份模式，如 logstash-2023.01
    private static final Pattern MONTH_PATTERN = Pattern.compile(".*-(\\d{4})\\.(\\d{2}).*");
    // 日期模式，如 logstash-2023.01.15
    private static final Pattern DAY_PATTERN = Pattern.compile(".*-(\\d{4})\\.(\\d{2})\\.(\\d{2}).*");

    public Tuple2<Integer, List<TemplateHistoryView>> getCurrentUseTemplatePage(TemplatePageInfo templatePageInfo) {
        try {
            // 获取所有模板并转换为TemplateHistoryView
            List<TemplateHistoryView> allTemplates = getAllTemplates();
            
            // 过滤出当前使用的模板
            List<TemplateHistoryView> currentTemplates = filterCurrentTemplates(allTemplates);
            
            // 如果有关键词搜索，进一步过滤
            if (templatePageInfo != null && StringUtils.hasText(templatePageInfo.getKeyword())) {
                String keyword = templatePageInfo.getKeyword().toLowerCase();
                currentTemplates = currentTemplates.stream()
                    .filter(template -> 
                        (template.getName() != null && template.getName().toLowerCase().contains(keyword)) ||
                        (template.getIndexPatterns() != null && template.getIndexPatterns().toLowerCase().contains(keyword))
                    )
                    .collect(Collectors.toList());
            }
            
            // 获取总数
            int total = currentTemplates.size();
            
            // 如果没有数据，直接返回空列表
            if (total == 0) {
                return new Tuple2<>(0, new ArrayList<>());
            }

            // 分页处理
            if (templatePageInfo != null && templatePageInfo.getPageNum() != null && templatePageInfo.getPageSize() != null) {
                int pageNum = Math.max(1, templatePageInfo.getPageNum());
                int pageSize = Math.max(1, templatePageInfo.getPageSize());
                
                // 使用ListUtils.splitListBySize进行分页
                List<List<TemplateHistoryView>> pagedLists = ListUtils.splitListBySize(currentTemplates, pageSize);
                
                // 防止页码越界
                if (pageNum > pagedLists.size()) {
                    return new Tuple2<>(total, new ArrayList<>());
                }
                
                // 获取指定页的数据（注意索引从0开始，而页码从1开始）
                currentTemplates = pagedLists.get(pageNum - 1);
            }
            
            return new Tuple2<>(total, currentTemplates);
        } catch (Exception e) {
            log.error("获取当前使用模板分页信息失败", e);
            return new Tuple2<>(0, new ArrayList<>());
        }
    }
    
    /**
     * 过滤出当前使用的模板
     * @param allTemplates 所有模板
     * @return 当前使用的模板
     */
    private List<TemplateHistoryView> filterCurrentTemplates(List<TemplateHistoryView> allTemplates) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        int currentDay = now.getDayOfMonth();
        
        return allTemplates.stream()
            .filter(template -> {
                if (template.getName() == null || template.getIndexPatterns() == null) {
                    return false;
                }
                
                String indexPattern = template.getIndexPatterns();
                
                // 检查是否为当前日期的模板
                Matcher dayMatcher = DAY_PATTERN.matcher(indexPattern);
                if (dayMatcher.matches()) {
                    int year = Integer.parseInt(dayMatcher.group(1));
                    int month = Integer.parseInt(dayMatcher.group(2));
                    int day = Integer.parseInt(dayMatcher.group(3));
                    return year == currentYear && month == currentMonth && day == currentDay;
                }
                
                // 检查是否为当前月份的模板
                Matcher monthMatcher = MONTH_PATTERN.matcher(indexPattern);
                if (monthMatcher.matches()) {
                    int year = Integer.parseInt(monthMatcher.group(1));
                    int month = Integer.parseInt(monthMatcher.group(2));
                    return year == currentYear && month == currentMonth;
                }
                
                // 检查是否为当前年份的模板
                Matcher yearMatcher = YEAR_PATTERN.matcher(indexPattern);
                if (yearMatcher.matches()) {
                    int year = Integer.parseInt(yearMatcher.group(1));
                    return year == currentYear;
                }
                
                // 如果没有日期信息，默认不是当前模板
                return false;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 获取所有模板并转换为TemplateHistoryView
     * @return 模板列表
     */
    private List<TemplateHistoryView> getAllTemplates() {
        try {
            // 获取模板基本信息
            List<EsTemplateInfo> templateInfoList = elasticRealTemplateService.getTemplateList();
            
            // 转换为TemplateHistoryView
            List<TemplateHistoryView> result = new ArrayList<>();
            for (EsTemplateInfo info : templateInfoList) {
                TemplateHistoryView view = new TemplateHistoryView();
                view.setName(info.getName());
                view.setOrder(info.getOrder());
                
                // 处理索引模式
                if (info.getIndexPatterns() != null && !info.getIndexPatterns().isEmpty()) {
                    view.setIndexPatterns(String.join(", ", info.getIndexPatterns()));
                }
                
                // 获取模板详情
                try {
                    JSONObject templateDetail = elasticRealTemplateService.getOneTemplateDetail(info.getName());
                    if (templateDetail != null) {
                        // 设置详情内容
                        view.setContent(templateDetail.toJSONString());
                        
                        // 尝试提取更多信息
                        extractTemplateDetails(view, templateDetail);
                    }
                } catch (Exception e) {
                    log.warn("获取模板{}详情失败: {}", info.getName(), e.getMessage());
                }
                
                result.add(view);
            }
            
            return result;
        } catch (IOException e) {
            log.error("获取模板列表失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 从模板详情中提取更多信息
     * @param view 模板视图对象
     * @param templateDetail 模板详情JSON
     */
    private void extractTemplateDetails(TemplateHistoryView view, JSONObject templateDetail) {
        try {
            // 尝试提取滚动策略、保存策略等信息
            // 这里的实现取决于templateDetail的具体结构
            // 以下是示例代码，需要根据实际JSON结构调整
            
            if (templateDetail.containsKey("index_templates")) {
                JSONObject indexTemplate = templateDetail.getJSONArray("index_templates").getJSONObject(0)
                    .getJSONObject("index_template");
                
                if (indexTemplate.containsKey("template") && 
                    indexTemplate.getJSONObject("template").containsKey("settings") &&
                    indexTemplate.getJSONObject("template").getJSONObject("settings").containsKey("index")) {
                    
                    JSONObject settings = indexTemplate.getJSONObject("template")
                        .getJSONObject("settings").getJSONObject("index");
                    
                    // 提取生命周期策略
                    if (settings.containsKey("lifecycle")) {
                        JSONObject lifecycle = settings.getJSONObject("lifecycle");
                        if (lifecycle.containsKey("name")) {
                            view.setRollingPolicy(lifecycle.getString("name"));
                        }
                    }
                    
                    // 提取其他可能的信息
                    if (settings.containsKey("number_of_shards")) {
                        view.setIndexNum(settings.getString("number_of_shards"));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取模板{}详细信息失败: {}", view.getName(), e.getMessage());
        }
    }
}
