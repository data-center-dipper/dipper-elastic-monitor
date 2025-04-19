package com.dipper.monitor.enums.elastic;

/**
 * 定义索引操作类型及其相关信息。
 */
public enum IndexOperatorType {

    /**
     * 删除索引。
     */
    DELETE("DELETE", "","删除索引"),

    /**
     * 关闭索引。
     */
    CLOSE("POST", "_close", "关闭索引"),

    /**
     * 开启索引。
     */
    OPEN("POST", "_open", "开启索引"),

    /**
     * 冻结索引。
     */
    FREEZE("POST", "_freeze", "冻结索引");

    private final String method;
    private final String operation;
    private final String description;

    /**
     * 构造函数初始化索引操作类型、操作命令及描述信息。
     *
     * @param method HTTP请求方法。
     * @param operation 索引操作命令。
     * @param description 操作功能描述。
     */
    IndexOperatorType(String method, String operation, String description) {
        this.method = method;
        this.operation = operation;
        this.description = description;
    }

    /**
     * 获取HTTP请求方法。
     *
     * @return 返回HTTP请求方法。
     */
    public String getMethod() {
        return method;
    }

    /**
     * 获取索引操作命令。
     *
     * @return 返回索引操作命令。
     */
    public String getOperation() {
        return operation;
    }

    /**
     * 获取操作的功能描述。
     *
     * @return 返回操作的功能描述。
     */
    public String getDescription() {
        return description;
    }
}