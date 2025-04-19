package com.dipper.monitor.utils;

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
}
