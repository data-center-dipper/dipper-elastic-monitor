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
