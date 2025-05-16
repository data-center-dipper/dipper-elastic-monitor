package com.dipper.monitor.utils.elastic;

import lombok.extern.slf4j.Slf4j;

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

}
