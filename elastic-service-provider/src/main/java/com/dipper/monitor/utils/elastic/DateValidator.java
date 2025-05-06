package com.dipper.monitor.utils.elastic;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateValidator {

    private static final Logger log = LoggerFactory.getLogger(DateValidator.class);

    // 定义日期格式与长度的映射
    private static final Map<Integer, String> DATE_FORMATS = new HashMap<>();
    static {
        DATE_FORMATS.put(8, "yyyyMMdd");
        DATE_FORMATS.put(6, "yyyyMM");
        DATE_FORMATS.put(4, "yyyy");
    }

    /**
     * 检查给定的日期字符串是否表示一个非未来的日期。
     *
     * @param pt 日期字符串。
     * @return 如果日期不是未来的，则返回 true；否则返回 false。
     */
    public boolean isNotFuture(String pt) {
        if (pt == null || pt.isEmpty()) {
            log.warn("日期字符串为空");
            return false;
        }

        FastDateFormat dateFormat = getDateFormatByLength(pt.length());
        if (dateFormat == null) {
            log.error("未知的日期格式：{}", pt);
            return false;
        }

        try {
            Date parsedDate = dateFormat.parse(pt);
            return !parsedDate.after(new Date());
        } catch (ParseException e) {
            log.error("日期解析失败：{}, 错误: {}", pt, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据日期字符串的长度获取相应的日期格式。
     *
     * @param length 日期字符串的长度。
     * @return 对应的 FastDateFormat 实例，如果没有匹配的格式则返回 null。
     */
    private FastDateFormat getDateFormatByLength(int length) {
        String format = DATE_FORMATS.get(length);
        return format != null ? FastDateFormat.getInstance(format) : null;
    }
}