package com.dipper.monitor.constants;

public enum PluginConstants {
    ELASTICSEARCH("elasticsearch","elasticsearch.properties", "elasticsearch-plugin-config"),
    REDIS("redis","redis.properties", "redis-plugin-config"),
    KAFKA("kafka","kafka.properties", "kafka-plugin-config"),
    MINIO("minio","minio.properties", "minio-config"),
    FLINK("flink","flink.properties", "flink-plugin-config"),
    ZOOKEEPER("zookeeper","zookeeper.properties", "zookeeper-plugin-config"),
    PLUGINS_JAR("plugins","plugins-jar.properties", "plugins-jar");

    final String pluginName;

    final String configFile;

    final String configKey;

    PluginConstants(String pluginName,String configFile, String configKey) {
        this.pluginName = pluginName;
        this.configFile = configFile;
        this.configKey = configKey;
    }

    public String getConfigFile() {
        return this.configFile;
    }

    public String getConfigKey() {
        return this.configKey;
    }

    public String getPluginName() {
        return pluginName;
    }
}
