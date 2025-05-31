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

    public static double bytesToGB(String bytes) {
        if(StringUtils.isBlank(bytes)){
            return 0;
        }
        return Math.round((Double.parseDouble(bytes) / (1024.0 * 1024.0 * 1024.0)) * 100.0) / 100.0;
    }


    public static Double bytesToMB(Long bytes) {
        return Math.round((bytes / (1024.0 * 1024.0)) * 100.0) / 100.0;
    }
}
