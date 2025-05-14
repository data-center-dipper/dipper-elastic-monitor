package com.dipper.monitor.service.elastic.template.impl.handlers.preview;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreviewCanRunTemplateHandler {

    /**
     * 替换模板中所有符合 yyyyMMdd 等格式的占位符为当前日期
     *
     * 示例输入：
     * {
     *   "aliases": {
     *     "logs-yyyyMMdd": {
     *       "is_write_index": true
     *     }
     *   },
     *   "index_patterns": "logs-yyyyMMdd-*"
     * }
     *
     * 输出：
     * {
     *   "aliases": {
     *     "logs-20250514": {
     *       "is_write_index": true
     *     }
     *   },
     *   "index_patterns": "logs-20250514-*"
     * }
     *
     * @param jsonObject 原始模板 JSON 对象
     * @return 替换后可运行的 JSON 模板
     */
    public JSONObject previewCanRunTemplate(JSONObject jsonObject) {
        String today = getCurrentDateFormatted();
        return replacePlaceholders(jsonObject, today);
    }

    // 可选：支持传入特定时间
    public JSONObject previewCanRunTemplate(JSONObject jsonObject, String customDate) {
        return replacePlaceholders(jsonObject, customDate);
    }

    private JSONObject replacePlaceholders(JSONObject jsonObject, String dateStr) {
        if (jsonObject == null) return null;

        // 处理 aliases
        JSONObject aliases = jsonObject.getJSONObject("aliases");
        if (aliases != null) {
            JSONObject newAliases = new JSONObject();
            for (String key : aliases.keySet()) {
                String newKey = replaceDatePlaceholder(key, dateStr);
                newAliases.put(newKey, aliases.get(key));
            }
            jsonObject.put("aliases", newAliases);
        }

        // 处理 index_patterns 字段
        Object patternObj = jsonObject.get("index_patterns");
        if (patternObj instanceof String) {
            String newPattern = replaceDatePlaceholder((String) patternObj, dateStr);
            jsonObject.put("index_patterns", newPattern);
        }

        return jsonObject;
    }

    /**
     * 替换字符串中的日期占位符
     *
     * 支持格式：
     * yyyy
     * yyyyMM
     * yyyyMMdd
     * yyyyMMddHH
     */
    private String replaceDatePlaceholder(String input, String dateStr) {
        if (input == null || dateStr == null) return input;

        // 正则匹配各种格式
        Pattern pattern = Pattern.compile("(yyyy)(MM)?(dd)?(HH)?");
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
    private String formatDate(String baseDate, String format) {
        try {
            LocalDateTime date = LocalDateTime.parse(baseDate, DateTimeFormatter.ISO_DATE_TIME);
            switch (format) {
                case "yyyy":
                    return String.valueOf(date.getYear());
                case "yyyyMM":
                    return String.format("%d%02d", date.getYear(), date.getMonthValue());
                case "yyyyMMdd":
                    return String.format("%d%02d%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
                case "yyyyMMddHH":
                    return String.format("%d%02d%02d%02d", date.getYear(), date.getMonthValue(),
                            date.getDayOfMonth(), date.getHour());
                default:
                    return format; // 不识别的格式保留原样
            }
        } catch (Exception e) {
            return formatWithSystemTime(format); // fallback to current time
        }
    }

    /**
     * 如果未传入时间，则使用系统时间作为默认值
     */
    private String getCurrentDateFormatted() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private String formatWithSystemTime(String format) {
        LocalDateTime now = LocalDateTime.now();
        switch (format) {
            case "yyyy":
                return String.valueOf(now.getYear());
            case "yyyyMM":
                return String.format("%d%02d", now.getYear(), now.getMonthValue());
            case "yyyyMMdd":
                return String.format("%d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
            case "yyyyMMddHH":
                return String.format("%d%02d%02d%02d", now.getYear(), now.getMonthValue(),
                        now.getDayOfMonth(), now.getHour());
            default:
                return format;
        }
    }
}