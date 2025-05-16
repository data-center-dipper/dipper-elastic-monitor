package com.dipper.monitor.utils.elastic;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class IndexPatternsUtils {

    /**
     * indexPatterns 格式有以下几种样式
     * lcc-logs-yyyyMMdd-*
     * lcc--yyyyMMdd-*
     * lcc--yyyyMMdd
     *
     * 清解析成
     *
     * lcc-logs-yyyyMMdd
     * lcc--yyyyMMdd
     * lcc--yyyyMMdd
     *
     * @return
     */
    public static String getIndexPrefixHaveDate(String indexPatterns) {
        if (indexPatterns == null || indexPatterns.isEmpty()) {
            log.error("indexPatterns 为空");
            return "";
        }

        String prefix = indexPatterns;

        // 去除 -* 或 *
        if (prefix.endsWith("-*")) {
            prefix = prefix.substring(0, prefix.length() - 2);
        } else if (prefix.endsWith("*")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }

        // 去除结尾可能的连续 -
        prefix = prefix.replaceAll("-+$", "");

        // 确保包含 yyyyMMdd
        if (!prefix.contains("yyyyMMdd")) {
            log.warn("indexPatterns 不包含 yyyyMMdd: {}", indexPatterns);
            return "";
        }

        return prefix;
    }

    /**
     * 获取索引前缀，去除 yyyyMMdd 时间格式部分，并清理结尾的特殊符号。
     * 支持格式：
     * - lcc-logs-yyyyMMdd-*
     * - lcc-yyyyMMdd-*
     * - lcc-yyyyMMdd
     *
     * @return 清理后的前缀，如 "lcc-logs" 或 "lcc"
     */
    public static String getIndexPrefixNoDateAndTail(String indexPatterns) {
        if (indexPatterns == null || indexPatterns.isEmpty()) {
            log.error("indexPatterns 为空");
            return "";
        }

        int dateIndex = indexPatterns.indexOf("yyyyMMdd");
        if (dateIndex == -1) {
            log.warn("indexPatterns 中未找到 'yyyyMMdd'：{}", indexPatterns);
            return "";
        }

        String prefix = indexPatterns.substring(0, dateIndex);

        // 去除结尾的 -, _, 空格等
        return prefix.replaceAll("[-_\\s]+$", "");
    }



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


}
