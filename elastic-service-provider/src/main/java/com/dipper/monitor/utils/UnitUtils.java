package com.dipper.monitor.utils;

import org.apache.commons.lang3.StringUtils;

public class UnitUtils {

    /**
     * 单位为字节(Byte)
     *
     * // 如果不足1kb 那么展示kb
     *         // 如果不足1mb 那么展示mb
     *         // 如果不足1gb 那么展示gb
     */
    public static String transToGbOrMB(long bytes) {
        double value = bytes;
        String unit = "B";

        if (bytes >= 1024) {
            value /= 1024;
            unit = "KB";
        }
        if (value >= 1024) {
            value /= 1024;
            unit = "MB";
        }
        if (value >= 1024) {
            value /= 1024;
            unit = "GB";
        }

        return String.format("%.2f %s", value, unit);
    }


    // 转成 GB（保留两位小数）
    public static double bytesToGB(long bytes) {
        return Math.round((bytes / (1024.0 * 1024.0 * 1024.0)) * 100.0) / 100.0;
    }

    public static double bytesToGB(Double bytes) {
        return Math.round((bytes / (1024.0 * 1024.0 * 1024.0)) * 100.0) / 100.0;
    }


    /**
     * 将带单位的存储大小字符串转换为 GB（保留两位小数）
     *
     * 支持格式示例：
     * - "681b", "1.2kb", "3.5MB", "2.1GB"
     * - "1024"（默认单位是字节）
     */
    public static double bytesToGB(String sizeStr) {
        if (StringUtils.isBlank(sizeStr)) {
            return 0.0;
        }

        // 提取数值和单位
        ValueUnitPair pair = extractValueAndUnit(sizeStr.trim());

        if (pair == null || Double.isNaN(pair.value)) {
            return 0.0;
        }

        // 转换为 GB
        return convertToGB(pair.value, pair.unit);
    }

    /**
     * 提取数值和单位
     */
    private static ValueUnitPair extractValueAndUnit(String input) {
        // 匹配类似 "123", "1.2kb", "3.5MB", "2.1GB" 的格式
        if (input == null || input.isEmpty()) {
            return null;
        }

        // 尝试匹配结尾的单位
        String lowerInput = input.toLowerCase();
        String unit = "b"; // 默认单位为字节

        // 检查是否有常见单位后缀
        if (lowerInput.endsWith("gb")) {
            unit = "g";
            input = lowerInput.substring(0, lowerInput.length() - 2);
        } else if (lowerInput.endsWith("mb")) {
            unit = "m";
            input = lowerInput.substring(0, lowerInput.length() - 2);
        } else if (lowerInput.endsWith("kb")) {
            unit = "k";
            input = lowerInput.substring(0, lowerInput.length() - 2);
        } else if (lowerInput.endsWith("b")) {
            unit = "b";
            input = lowerInput.substring(0, lowerInput.length() - 1);
        }

        // 去掉可能残留的非数字字符（如空格）
        input = input.replaceAll("[^\\d.]", "");

        try {
            double value = Double.parseDouble(input);
            return new ValueUnitPair(value, unit);
        } catch (NumberFormatException e) {
            return new ValueUnitPair(Double.NaN, null);
        }
    }

    /**
     * 根据单位将值转换为 GB（保留两位小数）
     */
    private static double convertToGB(double value, String unit) {
        switch (unit) {
            case "g": // GB 直接返回
                return roundTo2Decimals(value);
            case "m": // MB -> GB
                return roundTo2Decimals(value / 1024);
            case "k": // KB -> GB
                return roundTo2Decimals(value / (1024 * 1024));
            case "b": // B -> GB
                return roundTo2Decimals(value / (1024 * 1024 * 1024));
            default:
                return 0.0;
        }
    }

    /**
     * 辅助类：保存提取出的数值和单位
     */
    private static class ValueUnitPair {
        double value;
        String unit;

        ValueUnitPair(double value, String unit) {
            this.value = value;
            this.unit = unit;
        }
    }

    /**
     * 保留两位小数
     */
    private static double roundTo2Decimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static Double bytesToMB(Long bytes) {
        return Math.round((bytes / (1024.0 * 1024.0)) * 100.0) / 100.0;
    }
}
