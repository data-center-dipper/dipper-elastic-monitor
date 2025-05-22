package com.dipper.monitor.utils.elastic;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class IndexPatternsUtils {


    /**
     * 替换字符串中的日期占位符
     */
    public static String replaceDatePlaceholder(String input, String dateStr) {
        if (input == null || dateStr == null) return input;

        // 正则匹配各种格式
        Pattern pattern = Pattern.compile("yyyy(?!\\d)|MM(?!\\d)|dd(?!\\d)|HH(?!\\d)|yyyyMM|yyyyMMdd");
        Matcher matcher = pattern.matcher(input);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String format = matcher.group();
            String replacement = formatDate(dateStr, format);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 根据格式生成实际日期字符串
     */
    private static String formatDate(String baseDate, String format) {
        try {
            DateTimeFormatter formatterInput = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(baseDate, formatterInput);

            switch (format) {
                case "yyyy":
                    return String.valueOf(date.getYear());
                case "MM":
                    return String.format("%02d", date.getMonthValue());
                case "dd":
                    return String.format("%02d", date.getDayOfMonth());
                case "HH":
                    return "00"; // 假设没有小时信息，返回默认值
                case "yyyyMM":
                    return String.format("%d%02d", date.getYear(), date.getMonthValue());
                case "yyyyMMdd":
                    return date.format(formatterInput);
                default:
                    return format; // 不识别的格式保留原样
            }
        } catch (DateTimeParseException e) {
            return formatWithSystemTime(format); // fallback to current time
        }
    }

    /**
     * 使用系统当前时间作为回退方案
     */
    private static String formatWithSystemTime(String format) {
        LocalDate now = LocalDate.now();
        switch (format) {
            case "yyyy":
                return String.valueOf(now.getYear());
            case "MM":
                return String.format("%02d", now.getMonthValue());
            case "dd":
                return String.format("%02d", now.getDayOfMonth());
            case "yyyyMM":
                return String.format("%d%02d", now.getYear(), now.getMonthValue());
            case "yyyyMMdd":
                return now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            default:
                return format; // 不识别的格式保留原样
        }
    }


    public static String getIndexPrefixNoDate(String indexPatterns) {
        if(StringUtils.isEmpty(indexPatterns)){
            return "";
        }
        if(indexPatterns.contains("yyyyMMdd")){
            return getDayIndexPrefixNoDate(indexPatterns,"yyyyMMdd");
        }else if(indexPatterns.contains("yyyyMM")){
            return getMonthIndexPrefixNoDate(indexPatterns,"yyyyMM");
        }else if(indexPatterns.contains("yyyy")){
            return getYearIndexPrefixNoDate(indexPatterns,"yyyy");
        }else {
            return indexPatterns;
        }
    }

    private static String getYearIndexPrefixNoDate(String indexPatterns, String yyyy) {
        int i = indexPatterns.indexOf(yyyy);
        return indexPatterns.substring(0, i);
    }

    private static String getMonthIndexPrefixNoDate(String indexPatterns, String yyyyMM) {
        int i = indexPatterns.indexOf(yyyyMM);
        return indexPatterns.substring(0, i);
    }

    private static String getDayIndexPrefixNoDate(String indexPatterns, String yyyyMMdd) {
        int i = indexPatterns.indexOf(yyyyMMdd);
        return indexPatterns.substring(0, i);
    }

    public static String getIndexDatePattern(String indexPatterns) {
        if(StringUtils.isEmpty(indexPatterns)){
            return "";
        }
        if(indexPatterns.contains("yyyyMMdd")){
            return "yyyyMMdd";
        }else if(indexPatterns.contains("yyyyMM")){
            return "yyyyMM";
        }else if(indexPatterns.contains("yyyy")){
            return "yyyy";
        }else {
            return "";
        }
    }

    /**
     * 获取最近 n 天的日期列表。
     *
     * @param n       需要获取的天数。
     * @param pattern 输出的日期格式。
     * @return 包含最近 n 天日期的列表。
     */
    public static List<String> getLastNDays(int n, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        List<String> dates = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            // 插入到列表头部，保证最近的日期在前面
            dates.add(0, date.format(formatter));
        }
        return dates;
    }

    /**
     * 获取最近 n 个月的月份列表。
     *
     * @param n       需要获取的月份数。
     * @param pattern 输出的日期格式。
     * @return 包含最近 n 个月份的列表。
     */
    public static List<String> getLastNMonths(int n, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        List<String> dates = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            LocalDate date = LocalDate.now().minusMonths(i);
            // 插入到列表头部，保证最近的月份在前面
            dates.add(0, date.format(formatter));
        }
        return dates;
    }

    /**
     * 获取最近 n 年的年份列表。
     *
     * @param n       需要获取的年数。
     * @param pattern 输出的日期格式。
     * @return 包含最近 n 年的列表。
     */
    public static List<String> getLastNYears(int n, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        List<String> dates = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            LocalDate date = LocalDate.now().minusYears(i);
            // 插入到列表头部，保证最近的年份在前面
            dates.add(0, date.format(formatter));
        }
        return dates;
    }


}
