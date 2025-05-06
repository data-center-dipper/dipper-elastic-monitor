package com.dipper.monitor.enums.common;

public enum ModuleNameEnum {

    KAFKA("KAFKA", "kafka组件"),
    ZOOKEEPER("ZOOKEEPER", "zk组件"),
    ELASTICSEARCH("ELASTICSEARCH", "ELASTICSEARCH组件");

    public String module;
    public String message;

    ModuleNameEnum(String module, String message) {
        this.module = module;
        this.message = message;
    }
}
