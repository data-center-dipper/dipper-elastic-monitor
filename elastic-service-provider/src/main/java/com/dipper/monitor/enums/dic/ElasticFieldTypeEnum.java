package com.dipper.monitor.enums.dic;

public enum ElasticFieldTypeEnum {
    TEXT("text"),
    KEYWORD("keyword"),
    INTEGER("integer"),
    FLOAT("float"),
    DOUBLE("double"),
    DATE("date"),
    BOOLEAN("boolean");

    private final String typeName;

    ElasticFieldTypeEnum(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    // 根据类型名称获取枚举值的方法
    public static ElasticFieldTypeEnum fromTypeName(String typeName) {
        for (ElasticFieldTypeEnum type : ElasticFieldTypeEnum.values()) {
            if (type.getTypeName().equalsIgnoreCase(typeName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant found for type name: " + typeName);
    }
}