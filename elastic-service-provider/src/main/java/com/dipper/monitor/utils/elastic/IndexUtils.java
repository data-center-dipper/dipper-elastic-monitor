package com.dipper.monitor.utils.elastic;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndexUtils {
    public static boolean isIndexNameContainSpecialChar(String index) {
        if (StringUtils.isBlank(index)) {
            return true;
        }
        if (index.contains("{") || index.contains("}") || index
                .contains("+") || index
                .contains("|") || index.contains("||") || index
                .contains("!") || index
                .contains("(") || index.contains(")") || index
                .contains("[") || index.contains("]") || index
                .contains("?")) {
            return true;
        }
        return false;
    }

    public static List<String> getAliansListFromAliansSet(JSONObject aliansJson) {
        List<String> aliansList = new ArrayList<>();
        JSONObject jsonObject = (JSONObject) JSONPath.eval(aliansJson, "$.aliases");
        for (Map.Entry<String, Object> item : (Iterable<Map.Entry<String, Object>>)jsonObject.entrySet()) {
            String aliansName = item.getKey();
            aliansList.add(aliansName);
        }
        return aliansList;
    }

    /**
     *
     * @param index
     * @param indexPatterns 格式可能是
     *                      xxx-xx-aa-yyyy
     *                      xxx-xx-aa-yyyy-*
     *                      xxx-xx-aa-yyyy*
     *                      xxx-xx-aa-yyyyMM
     *                      xxx-xx-aa-yyyyMM-*
     *                      xxx-xx-aa-yyyyMM*
     *                      xxx-xx-aa-yyyyMMdd
     *                      xxx-xx-aa-yyyyMMdd-*
     *                      xxx-xx-aa-yyyyMMdd*
     *                      xxx-xx-aa-*
     * @return
     */
    public static boolean isMatchPattern(String index, String indexPatterns) {
        if (StringUtils.isBlank(index) || StringUtils.isBlank(indexPatterns)) {
            return false;
        }

        // 1. 如果 pattern 是纯通配符 "*"，直接返回 true
        if ("*".equals(indexPatterns)) {
            return true;
        }

        // 2. 如果 pattern 以 "*"" 结尾，则进行前缀匹配
        if (indexPatterns.endsWith("*")) {
            String prefix = indexPatterns.substring(0, indexPatterns.length() - 1);
            return index.startsWith(prefix);
        }

        // 3. 替换动态部分为正则表达式
        String regex = indexPatterns;

        // 替换 yyyy -> \d{4}
        regex = regex.replaceAll("yyyy", "\\d{4}");

        // 替换 MM -> 01-12
        regex = regex.replaceAll("MM", "0[1-9]|1[0-2]");

        // 替换 dd -> 01-31
        regex = regex.replaceAll("dd", "0[1-9]|[12][0-9]|3[01]");

        // 将普通符号转义
        regex = regex.replace(".", "\\.");

        // 构建完整正则表达式并匹配
        Pattern pattern = Pattern.compile("^" + regex + "$");
        Matcher matcher = pattern.matcher(index);

        return matcher.matches();
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /**
     * 判断索引名是否在指定时间范围内
     */
    public static boolean isInDateRange(String index, String startTimeStr, String endTimeStr) {
        if (StringUtils.isBlank(startTimeStr) && StringUtils.isBlank(endTimeStr)) {
            return true;
        }

        // 提取索引名中的日期部分（支持 yyyy.MM.dd / yyyyMMdd / yyyyMM）
        Pattern datePattern = Pattern.compile("(\\d{4}\\.\\d{2}\\.\\d{2}|\\d{8}|\\d{6})");
        Matcher matcher = datePattern.matcher(index);

        if (!matcher.find()) {
            return false; // 没有日期格式的部分
        }

        String dateString = matcher.group(1);

        LocalDate date = null;
        try {
            if (dateString.length() == 8 && dateString.matches("\\d{8}")) {
                date = LocalDate.parse(dateString, DateTimeFormatter.BASIC_ISO_DATE);
            } else if (dateString.length() == 6 && dateString.matches("\\d{6}")) {
                // 补全成 202505 -> 2025.05.01
                date = LocalDate.parse(dateString + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));
            } else if (dateString.length() == 10) {
                date = LocalDate.parse(dateString, DATE_FORMATTER);
            } else {
                return false;
            }
        } catch (DateTimeParseException e) {
            return false;
        }

        LocalDate now = LocalDate.now();
        LocalDate start = StringUtils.isNotBlank(startTimeStr) ? LocalDate.parse(startTimeStr, DATE_FORMATTER) : null;
        LocalDate end = StringUtils.isNotBlank(endTimeStr) ? LocalDate.parse(endTimeStr, DATE_FORMATTER) : null;

        boolean afterStart = start == null || !date.isBefore(start);
        boolean beforeEnd = end == null || !date.isAfter(end);

        return afterStart && beforeEnd;
    }
}