package com.dipper.monitor.enums.props;

public enum PropsEnum {

    ES_DISK_CLEAR_LOW("es", "disk.clear", "", "es.disk.clear.low", "低阈值","80"),
    ES_DISK_CLEAR_MIDDLE("es", "disk.clear", "", "es.disk.clear.middle", "中阈值","90"),
    ES_DISK_CLEAR_HIGH("es", "disk.clear", "", "es.disk.clear.high", "高阈值","95");

    public String moduleName;
    public String entityName;
    public String sectionName;

    public String configKey;
    public String configName;

    public String defaultValue;

    PropsEnum(String moduleName, String entityName, String sectionName,
              String configKey, String configName, String defaultValue) {
        this.moduleName = moduleName;
        this.entityName = entityName;
        this.sectionName = sectionName;
        this.configKey = configKey;
        this.configName = configName;
        this.defaultValue = defaultValue;
    }
}
