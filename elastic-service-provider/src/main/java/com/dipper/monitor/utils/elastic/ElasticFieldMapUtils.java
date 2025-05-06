package com.dipper.monitor.utils.elastic;

import com.dipper.monitor.enums.dic.ComTypeEnum;
import com.dipper.monitor.enums.dic.ElasticFieldTypeEnum;

import java.util.List;
import java.util.stream.Collectors;

public class ElasticFieldMapUtils {

    /**
     * 检查 fieldType 是否不为空，并且必须是 ComTypeEnum 中的一个。
     * @param fieldType 字段类型字符串
     */
    public static void checkFieldType(String fieldType) {
        if (fieldType == null || fieldType.trim().isEmpty()) {
            throw new IllegalArgumentException("Field type cannot be null or empty.");
        }

        boolean isValid = false;
        for (ComTypeEnum type : ComTypeEnum.values()) {
            if (type.getTypeName().equalsIgnoreCase(fieldType.trim())) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new IllegalArgumentException("Invalid field type: " + fieldType + ". It must be one of the ComTypeEnum constants.");
        }
    }

    /**
     * 检查 fieldType 是否不为空，并且必须是 ElasticFieldTypeEnum 中的一个。
     * @param esMappingType Elasticsearch字段类型字符串
     */
    public static void checkEsFiledType(String esMappingType) {
        if (esMappingType == null || esMappingType.trim().isEmpty()) {
            throw new IllegalArgumentException("Elasticsearch field type cannot be null or empty.");
        }

        boolean isValid = false;
        for (ElasticFieldTypeEnum type : ElasticFieldTypeEnum.values()) {
            if (type.getTypeName().equalsIgnoreCase(esMappingType.trim())) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new IllegalArgumentException("Invalid Elasticsearch field type: " + esMappingType + ". It must be one of the ElasticFieldTypeEnum constants.");
        }
    }

    /**
     * 传入的是普通的类型，请转成es专用类型
     * @param fieldType 普通数据库字段类型字符串
     * @return Elasticsearch字段类型字符串
     */
    public static String autoEsTypeMap(String fieldType) {
        // 首先检查fieldType是否有效
        checkFieldType(fieldType);

        // 将输入的fieldType转换为ComTypeEnum
        ComTypeEnum comTypeEnum = null;
        for (ComTypeEnum type : ComTypeEnum.values()) {
            if (type.getTypeName().equalsIgnoreCase(fieldType.trim())) {
                comTypeEnum = type;
                break;
            }
        }

        if (comTypeEnum == null) {
            throw new IllegalArgumentException("Invalid field type: " + fieldType);
        }

        // 根据ComTypeEnum转换为ElasticFieldTypeEnum
        ElasticFieldTypeEnum elasticFieldType = transToElasticType(comTypeEnum);

        // 返回Elasticsearch字段类型的名称
        return elasticFieldType.getTypeName();
    }


    /**
     * 将普通类型映射到ES类型上
     * @param comTypeEnum 普通数据库字段类型
     * @return ES字段类型
     */
    public static ElasticFieldTypeEnum transToElasticType(ComTypeEnum comTypeEnum){
        switch (comTypeEnum) {
            case VARCHAR:
            case TEXT:
                return ElasticFieldTypeEnum.TEXT;
            case INTEGER:
                return ElasticFieldTypeEnum.INTEGER;
            case BIGINT:
                // 如果需要区分BIGINT和INTEGER在ES中也可以直接返回INTEGER
                return ElasticFieldTypeEnum.INTEGER;
            case FLOAT:
                return ElasticFieldTypeEnum.FLOAT;
            case DOUBLE:
                // 如果需要区分DOUBLE和FLOAT在ES中也可以直接返回FLOAT或定义新的类型
                return ElasticFieldTypeEnum.FLOAT;
            case DATE:
            case DATETIME:
                return ElasticFieldTypeEnum.DATE;
            case BOOLEAN:
                return ElasticFieldTypeEnum.BOOLEAN;
            case TINYINT:
                // TINYINT可以视情况映射到BOOLEAN或INTEGER
                return ElasticFieldTypeEnum.INTEGER;
            case DECIMAL:
                // DECIMAL可以根据需要映射到FLOAT或DOUBLE
                return ElasticFieldTypeEnum.DOUBLE;
            default:
                throw new IllegalArgumentException("Unsupported type: " + comTypeEnum.getTypeName());
        }
    }


    /**
     * 将多个普通类型映射到ES类型上
     * @param comTypeEnums 普通数据库字段类型的列表
     * @return ES字段类型的列表
     */
    public static List<ElasticFieldTypeEnum> transToElasticTypes(List<ComTypeEnum> comTypeEnums){
        return comTypeEnums.stream()
                .map(ElasticFieldMapUtils::transToElasticType)
                .collect(Collectors.toList());
    }
}