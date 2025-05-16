package com.dipper.monitor.service.elastic.template.impl.handlers.preview;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.utils.elastic.IndexPatternsUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preview7xCanRunTemplateHandler extends AbstractPreviewHandler {

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
        JSONObject aliases = jsonObject.getJSONObject("aliases");
        if (aliases != null) {
            JSONObject newAliases = new JSONObject();
            for (String key : aliases.keySet()) {
                String newKey = IndexPatternsUtils.replaceDatePlaceholder(key, dateStr);
                newAliases.put(newKey, aliases.get(key));
            }
            jsonObject.put("aliases", newAliases);
        }

        // 处理 index_patterns 字段
        Object patternObj = jsonObject.get("index_patterns");
        if (patternObj instanceof String) {
            String newPattern = IndexPatternsUtils.replaceDatePlaceholder((String) patternObj, dateStr);
            jsonObject.put("index_patterns", newPattern);
        }

        return jsonObject;
    }



    /**
     * 如果未传入时间，则使用系统时间作为默认值
     */
    private String getCurrentDateFormatted() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ISO_DATE_TIME);
    }

}