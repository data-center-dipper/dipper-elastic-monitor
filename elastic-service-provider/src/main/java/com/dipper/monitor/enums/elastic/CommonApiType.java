package com.dipper.monitor.enums.elastic;

/**
 * 定义API接口的类型及其说明。
 */
public enum CommonApiType {

    /**
     * 不需要添加参数的API接口。
     */
    COMMON("COMMON", "不需要添加参数的"),

    /**
     * 需要添加参数的API接口。
     */
    PARAMETER("PARAMETER", "需要添加参数的"),

    /**
     * 常用后端代码封装的接口。
     */
    ENCAPSULATION("ENCAPSULATION", "常用后端代码封装的接口"),

    /**
     * 自定义的API接口，用户需要填写命令。
     */
    SELF("SELF", "自定义的api, 需要用户填写命令");

    private final String type;
    private final String message;

    /**
     * 构造函数初始化API类型和对应的描述信息。
     *
     * @param type   API类型的标识符。
     * @param message 对应API类型的描述信息。
     */
    CommonApiType(String type, String message) {
        this.type = type;
        this.message = message;
    }

    /**
     * 获取API类型的标识符。
     *
     * @return 返回API类型的标识符。
     */
    public String getType() {
        return this.type;
    }

    /**
     * 获取API类型的描述信息。
     *
     * @return 返回API类型的描述信息。
     */
    public String getMessage() {
        return this.message;
    }
}