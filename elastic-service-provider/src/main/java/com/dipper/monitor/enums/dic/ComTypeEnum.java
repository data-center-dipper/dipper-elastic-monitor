package com.dipper.monitor.enums.dic;

public enum ComTypeEnum {
    VARCHAR("VARCHAR"),
    TEXT("TEXT"),
    INTEGER("INTEGER"),
    BIGINT("BIGINT"),
    FLOAT("FLOAT"),
    DOUBLE("DOUBLE"),
    DATE("DATE"),
    DATETIME("DATETIME"),
    BOOLEAN("BOOLEAN"),
    TINYINT("TINYINT"),
    DECIMAL("DECIMAL");

    private final String typeName;

    ComTypeEnum(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    // 根据类型名称获取枚举值的方法
    public static ComTypeEnum fromTypeName(String typeName) {
        for (ComTypeEnum type : ComTypeEnum.values()) {
            if (type.getTypeName().equalsIgnoreCase(typeName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant found for type name: " + typeName);
    }
}