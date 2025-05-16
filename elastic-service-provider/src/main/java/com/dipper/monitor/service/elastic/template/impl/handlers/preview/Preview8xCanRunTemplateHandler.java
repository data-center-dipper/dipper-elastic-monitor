package com.dipper.monitor.service.elastic.template.impl.handlers.preview;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preview8xCanRunTemplateHandler extends AbstractPreviewHandler {

    @Override
    protected void addOrUpdateMappings(JSONObject templateJson, String dicName) {
        // 这个处理器不需要实现映射添加逻辑，保留空实现
    }

    /**
     * 替换模板中所有符合 yyyyMMdd 等格式的占位符为当前日期
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
        JSONObject template = jsonObject.getJSONObject("template");
        JSONObject aliases = template.getJSONObject("aliases");
        if (aliases != null) {
            JSONObject newAliases = new JSONObject();
            for (String key : aliases.keySet()) {
                String newKey = replaceDatePlaceholder(key, dateStr);
                newAliases.put(newKey, aliases.get(key));
            }
            template.put("aliases", newAliases);
        }

        // 处理 index_patterns 字段
        JSONArray patternArray = jsonObject.getJSONArray("index_patterns");
        Iterator<Object> iterator = patternArray.iterator();
        JSONArray patternArrayResult = new JSONArray();
        while (iterator.hasNext()){
        String patternItem = (String) iterator.next();
            String newPattern = replaceDatePlaceholder(patternItem, dateStr);
            patternArrayResult.add(newPattern);
        }
        jsonObject.put("index_patterns", patternArrayResult);


        return jsonObject;
    }

    /**
     * 替换字符串中的日期占位符
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