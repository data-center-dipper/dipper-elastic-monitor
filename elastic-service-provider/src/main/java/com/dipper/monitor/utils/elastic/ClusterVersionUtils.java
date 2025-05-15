package com.dipper.monitor.utils.elastic;

public class ClusterVersionUtils {

    /**
     * 判断 Elasticsearch 版本是不是 7.x 版本
     * @param clusterVersion Elasticsearch 集群版本号，例如 "7.10.2"
     * @return 如果版本号属于 7.x 系列则返回 true，否则返回 false
     */
    public static boolean is7xVersion(String clusterVersion) {
        if (clusterVersion == null || !clusterVersion.startsWith("7.")) {
            return false;
        }
        // 检查是否为 7.x 格式
        String[] parts = clusterVersion.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            int majorVersion = Integer.parseInt(parts[0]);
            int minorVersion = Integer.parseInt(parts[1]);
            return majorVersion == 7 && minorVersion >= 0 && minorVersion <= 99; // 假设不会超过 7.99
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断 Elasticsearch 版本是不是 8.x 版本
     * @param clusterVersion Elasticsearch 集群版本号，例如 "8.2.0"
     * @return 如果版本号属于 8.x 系列则返回 true，否则返回 false
     */
    public static boolean is8xVersion(String clusterVersion) {
        if (clusterVersion == null || !clusterVersion.startsWith("8.")) {
            return false;
        }
        // 检查是否为 8.x 格式
        String[] parts = clusterVersion.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            int majorVersion = Integer.parseInt(parts[0]);
            int minorVersion = Integer.parseInt(parts[1]);
            return majorVersion == 8 && minorVersion >= 0 && minorVersion <= 99; // 假设不会超过 8.99
        } catch (NumberFormatException e) {
            return false;
        }
    }
}