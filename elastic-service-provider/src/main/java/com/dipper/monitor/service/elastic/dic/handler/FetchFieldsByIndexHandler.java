package com.dipper.monitor.service.elastic.dic.handler;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.dic.Field;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.utils.elastic.ElasticWordUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class FetchFieldsByIndexHandler {

    private ElasticRealIndexService elasticRealIndexService;

    public FetchFieldsByIndexHandler(ElasticRealIndexService elasticRealIndexService) {
        this.elasticRealIndexService = elasticRealIndexService;
    }

    /**
     * 根据索引名称获取字段列表
     *
     * @param indexName 索引名称
     * @return 字段列表
     */
    public List<Field> fetchFieldsByIndex(String indexName) {
        if (StringUtils.isBlank(indexName)) {
            log.warn("索引名称为空，无法提取字段");
            return new ArrayList<>();
        }

        try {
            // 获取 mapping JSON
            JSONObject mappingJson = elasticRealIndexService.getMappingByIndexName(indexName);
            if (mappingJson == null || mappingJson.isEmpty()) {
                log.warn("未找到索引 {} 的 mapping 数据", indexName);
                return new ArrayList<>();
            }

            // 提取 properties 字段定义部分
            JSONObject properties = mappingJson.getJSONObject("properties");
            if (properties == null) {
                log.warn("索引 {} 的 mapping 中没有 'properties' 字段定义", indexName);
                return new ArrayList<>();
            }


            // 解析字段并转换为 Field 对象
            List<Field> fieldList = new ArrayList<>();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldDef = entry.getValue();

                if (!(fieldDef instanceof JSONObject)) {
                    continue;
                }

                JSONObject fieldJson = (JSONObject) fieldDef;
                String esMappingType = fieldJson.getString("type");

                // 创建 Field 实体对象
                Field field = new Field();
                field.setZhName(fieldName); // 可以后续替换为中文映射名（如果有）
                field.setEnName(fieldName);
                field.setEsMappingType(esMappingType);

                // 转换 ES 类型为数据库类型（可按需扩展）
                field.setFieldType(ElasticWordUtils.convertEsTypeToDbType(esMappingType));

                fieldList.add(field);
            }

            return fieldList;

        } catch (Exception e) {
            log.error("从索引 {} 提取字段时发生错误", indexName, e);
            return new ArrayList<>();
        }
    }


}