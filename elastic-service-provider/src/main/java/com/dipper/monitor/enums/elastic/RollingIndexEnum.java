package com.dipper.monitor.enums.elastic;

public enum RollingIndexEnum {

    NONE("none", 0, "不需要滚动的"),
    DAILY("ONE", 1, "按天滚动的"),
    EVERY_2_DAYS("SECOND", 2, "2天滚动一次的"),
    EVERY_5_DAYS("FIVE_5", 5, "5天滚动一次的"),
    EVERY_10_DAYS("TEN", 10, "10天滚动一次的"),
    EVERY_15_DAYS("FIFTEEN", 15, "15天滚动一次的"),
    EVERY_30_DAYS("THIRTY", 30, "30天滚动一次的"),
    EVERY_60_DAYS("SIXTY", 60, "60天滚动一次的"),
    EVERY_180_DAYS("HALF_YEAR", 180, "半年滚动一次的"),
    EVERY_365_DAYS("YEAR", 365, "1年滚动一次的");

    private final String code;
    private final int days;
    private final String message;

    /**
     * 构造函数初始化滚动策略、对应的天数和描述信息。
     *
     * @param code   滚动策略的标识符。
     * @param days   滚动间隔的天数。
     * @param message 对应滚动策略的描述信息。
     */
    RollingIndexEnum(String code, int days, String message) {
        this.code = code;
        this.days = days;
        this.message = message;
    }

    /**
     * 获取滚动策略的标识符。
     *
     * @return 返回滚动策略的标识符。
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 获取滚动间隔的天数。
     *
     * @return 返回滚动间隔的天数。
     */
    public int getDays() {
        return this.days;
    }

    /**
     * 获取滚动策略的描述信息。
     *
     * @return 返回滚动策略的描述信息。
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * 根据code查找对应的枚举实例。
     *
     * @param code 滚动策略的标识符。
     * @return 返回找到的枚举实例；如果未找到，则返回null。
     */
    public static RollingIndexEnum fromCode(String code) {
        for (RollingIndexEnum rollingIndexEnum : values()) {
            if (rollingIndexEnum.getCode().equals(code)) {
                return rollingIndexEnum;
            }
        }
        return null;
    }

    /**
     * 根据天数查找对应的枚举实例。
     *
     * @param days 滚动间隔的天数。
     * @return 返回找到的枚举实例；如果未找到，则返回null。
     */
    public static RollingIndexEnum fromDays(int days) {
        for (RollingIndexEnum rollingIndexEnum : values()) {
            if (rollingIndexEnum.getDays() == days) {
                return rollingIndexEnum;
            }
        }
        return null;
    }
}