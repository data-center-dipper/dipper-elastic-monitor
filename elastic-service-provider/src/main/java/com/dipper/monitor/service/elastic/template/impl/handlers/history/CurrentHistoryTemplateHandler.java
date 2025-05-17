package com.dipper.monitor.service.elastic.template.impl.handlers.history;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.TemplatePageInfo;
import com.dipper.monitor.entity.elastic.template.history.EsTemplateInfo;
import com.dipper.monitor.entity.elastic.template.history.TemplateDetailView;
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
     * 过滤出当前使用的模板。
     * 根据 yyyyMMdd, yyyyMM, yyyy 格式的时间字符串进行匹配。
     *
     * @param allTemplates 所有模板列表
     * @return 符合当前时间条件的模板列表
     */
    private List<TemplateHistoryView> filterCurrentTemplates(List<TemplateHistoryView> allTemplates) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        int currentDay = now.getDayOfMonth();

        // 构造用于匹配的字符串
        String dayStr = String.format("%d%02d%02d", currentYear, currentMonth, currentDay);   // 20250517
        String monthStr = String.format("%d%02d", currentYear, currentMonth);                  // 202505
        String yearStr = String.valueOf(currentYear);

        int dayInt = Integer.parseInt(dayStr);// 2025
        int monthInt = Integer.parseInt(monthStr);// 2025
        int yearInt = Integer.parseInt(yearStr);// 2025

        List<TemplateHistoryView> result = new ArrayList<>();

        for (TemplateHistoryView template : allTemplates) {
            if (template.getName() == null || template.getIndexPatterns() == null) {
                continue;
            }

            String name = template.getName();

            // 跳过以点开头的系统模板（如 .monitoring-*）
            if (name.startsWith(".")) {
                continue;
            }

            // 如果不包含时间，那么直接跳过，说明没有时间信息
            if (!name.contains(yearStr)) {
                continue;
            }
            // 获取时间信息 ailpha-logs-20250518
            String dateTemplate = name.substring(name.lastIndexOf("-") + 1);
            Integer dateTemplateInt = Integer.parseInt(dateTemplate);


            if(dateTemplate.length() == 8){
              if(dayInt <= dateTemplateInt){
                result.add(template);
              }
            }

            if(dateTemplate.length() == 6){
                if(monthInt <= dateTemplateInt){
                    result.add(template);
                }
            }

            if(dateTemplate.length() == 4){
                if(yearInt <= dateTemplateInt){
                    result.add(template);
                }
            }
        }

        return result;
    }

    /**
     * 安全地从 Matcher 中提取分组内容并转换为整数
     *
     * @param matcher 正则匹配器
     * @param groupIndex 分组索引
     * @return 解析后的整数值，失败返回 -1
     */
    private int parseGroup(Matcher matcher, int groupIndex) {
        try {
            return Integer.parseInt(matcher.group(groupIndex));
        } catch (Exception e) {
            return -1; // 出现异常时返回无效值，便于判断
        }
    }
    

}
