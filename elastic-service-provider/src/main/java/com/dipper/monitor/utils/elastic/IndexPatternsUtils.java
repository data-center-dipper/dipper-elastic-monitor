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
        if (StringUtils.isEmpty(indexPatterns)) {
            return "";
        }
        if (indexPatterns.contains("yyyyMMdd")) {
            return getDayIndexPrefixNoDate(indexPatterns, "yyyyMMdd");
        } else if (indexPatterns.contains("yyyyMM")) {
            return getMonthIndexPrefixNoDate(indexPatterns, "yyyyMM");
        } else if (indexPatterns.contains("yyyy")) {
            return getYearIndexPrefixNoDate(indexPatterns, "yyyy");
        } else {
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
        if (StringUtils.isEmpty(indexPatterns)) {
            return "";
        }
        if (indexPatterns.contains("yyyyMMdd")) {
            return "yyyyMMdd";
        } else if (indexPatterns.contains("yyyyMM")) {
            return "yyyyMM";
        } else if (indexPatterns.contains("yyyy")) {
            return "yyyy";
        } else {
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


    // 支持的 datePattern 常量（可选）
    public static final String PATTERN_YYYYMMDD = "yyyyMMdd";
    public static final String PATTERN_YYYYMM = "yyyyMM";
    public static final String PATTERN_YYYY = "yyyy";
    public static final String PATTERN_YYYY_MM = "yyyy.MM";
    public static final String PATTERN_YYYY_MM_DD = "yyyy.MM.dd";

    /**
     * 从索引名称中提取日期信息并格式化为 "yyyy-MM-dd"
     *
     * @param index       索引名称
     * @param datePattern 日期格式标识符，取值为 yyyyMMdd、yyyyMM、yyyy、yyyy.MM、yyyy.MM.dd
     * @return 解析后的日期字符串
     */
    public static String extractDateFromIndexName(String index, String datePattern) {
        if (index == null || index.isEmpty()) {
            return null;
        }

        String[] parts = index.split("-");

        String part = parts[parts.length - 2];


        switch (datePattern) {
            case PATTERN_YYYYMMDD:
                return tryParseYYYYMMDD8(part);
            case PATTERN_YYYY_MM_DD:
                return tryParseYYYYDotMMDotDD(part);
            case PATTERN_YYYYMM:
                return tryParseYYYYMM6(part);
            case PATTERN_YYYY_MM:
                return tryParseYYYYDotMM(part);
            case PATTERN_YYYY:
                return tryParseYYYY4(part);
            default:
               return null;
        }
    }

    // 尝试解析 yyyyMMdd 格式（8位）
    private static String tryParseYYYYMMDD8(String part) {
        if (isLength(part, 8) && isNumeric(part)) {
            try {
                LocalDate date = LocalDate.parse(part, DateTimeFormatter.BASIC_ISO_DATE);
                return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    // 尝试解析 yyyy.MM.dd 格式（10位）
    private static String tryParseYYYYDotMMDotDD(String part) {
        if (isLength(part, 10)
                && part.charAt(4) == '.' && part.charAt(7) == '.') {
            String y = part.substring(0, 4);
            String m = part.substring(5, 7);
            String d = part.substring(8, 10);
            if (isNumeric(y) && isNumeric(m) && isNumeric(d)) {
                try {
                    int year = Integer.parseInt(y);
                    int month = Integer.parseInt(m);
                    int day = Integer.parseInt(d);
                    LocalDate date = LocalDate.of(year, month, day);
                    return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (NumberFormatException | DateTimeParseException ignored) {
                }
            }
        }
        return null;
    }

    // 尝试解析 yyyy.MM 格式（7位）
    private static String tryParseYYYYDotMM(String part) {
        if (isLength(part, 7)
                && part.charAt(4) == '.' && isNumeric(part.substring(0, 4))) {
            String y = part.substring(0, 4);
            String m = part.substring(5, 7);
            if (isNumeric(y) && isNumeric(m)) {
                try {
                    int year = Integer.parseInt(y);
                    int month = Integer.parseInt(m);
                    return String.format("%04d-%02d-01", year, month);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    // 尝试解析 yyyyMM 格式（6位）
    private static String tryParseYYYYMM6(String part) {
        if (isLength(part, 6) && isNumeric(part)) {
            String y = part.substring(0, 4);
            String m = part.substring(4, 6);
            if (isNumeric(y) && isNumeric(m)) {
                try {
                    int year = Integer.parseInt(y);
                    int month = Integer.parseInt(m);
                    return String.format("%04d-%02d-01", year, month);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    // 尝试解析 yyyy 格式（4位）
    private static String tryParseYYYY4(String part) {
        if (isLength(part, 4) && isNumeric(part)) {
            try {
                int year = Integer.parseInt(part);
                return String.format("%04d-01-01", year);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    // 判断字符串是否是纯数字
    private static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    // 判断字符串长度是否等于指定值
    private static boolean isLength(String str, int length) {
        return str != null && str.length() == length;
    }

}
