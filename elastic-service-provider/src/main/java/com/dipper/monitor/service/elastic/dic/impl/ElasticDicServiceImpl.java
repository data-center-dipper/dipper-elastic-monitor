package com.dipper.monitor.service.elastic.dic.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.dic.Dic;
import com.dipper.monitor.entity.elastic.dic.Field;
import com.dipper.monitor.service.elastic.dic.DicService;
import com.dipper.monitor.service.elastic.dic.ElasticDicService;
import com.dipper.monitor.service.elastic.dic.WordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ElasticDicServiceImpl implements ElasticDicService {

    @Autowired
    private DicService dicService;
    @Autowired
    private WordService wordService;

    @Override
    public JSONObject getElasticMapping(String dicName) {
        Dic dicByName = dicService.getDicByName(dicName);
        if (dicByName == null) {
            return null;
        }
        List<Field> fieldsByDicId = wordService.getFieldsByDicId(dicByName.getId());
        if(fieldsByDicId == null || fieldsByDicId.isEmpty()){
            return null;
        }

        JSONObject fieldMappingResult = new JSONObject();
        for (Field field : fieldsByDicId) {
            String enName = field.getEnName();
            String esMappingType = field.getEsMappingType();
            if (enName == null || enName.trim().isEmpty()) {
                continue;
            }
            if (esMappingType == null || esMappingType.trim().isEmpty()) {
                continue;
            }
            JSONObject fieldSetting = getFieldSetting(esMappingType);
            fieldMappingResult.put(enName, fieldSetting);
        }
        return fieldMappingResult;
    }

    /**
     * type: 定义字段的数据类型，如text, keyword, date, integer等。
     * fields: 允许为同一个字段定义多个子字段，比如对一个text字段同时定义其keyword子字段以便进行精确匹配或聚合操作。
     * ignore_above: 对于keyword类型，超过指定长度的值将不会被索引。
     * analyzer & search_analyzer: 分别指定索引时和查询时使用的分析器，如standard, english等预定义分析器，也可以是自定义分析器。
     * store: 如果设置为true，则会存储原始字段值，这允许你直接从索引中检索字段值而不必重建它。
     * term_vector: 支持快速高亮显示和其他特性，选项包括no, yes, with_offsets, with_positions, with_positions_offsets。
     * norms: 包含标准化数据（影响评分），如果字段不参与评分计算可设为false以节省空间。
     * index_options: 控制倒排索引记录的信息量，选项有docs, freqs, positions, offsets。
     */
    private JSONObject getFieldSetting(String esMappingType) {
        JSONObject fieldSetting = new JSONObject();
        switch (esMappingType) {
            case "text":
                fieldSetting = getTextSetting();
                break;
            case "keyword":
                fieldSetting = getKeywordSetting();
                break;
            case "date":
                fieldSetting = getDateSetting();
                break;
            case "integer":
                fieldSetting = getIntegerSetting();
                break;
            case "long":
                fieldSetting = getLongSetting();
                break;
            case "float":
                fieldSetting = getFloatSetting();
                break;
            case "double":
                fieldSetting = getDoubleSetting();
                break;
            default:
                throw new IllegalArgumentException("Invalid ES mapping type: " + esMappingType);
        }
        return fieldSetting;
    }

    private JSONObject getDoubleSetting() {
        JSONObject fieldSetting = new JSONObject();
        fieldSetting.put("type", "double");
        return fieldSetting;
    }

    private JSONObject getFloatSetting() {
        JSONObject fieldSetting = new JSONObject();
        fieldSetting.put("type", "float");
        return fieldSetting;
    }

    private JSONObject getLongSetting() {
        JSONObject fieldSetting = new JSONObject();
        fieldSetting.put("type", "long");
        return fieldSetting;
    }

    /**
     * doc_values: 对数值类型默认启用，有助于提高排序和聚合效率。
     * @return
     */
    private JSONObject getIntegerSetting() {
        JSONObject fieldSetting = new JSONObject();
        fieldSetting.put("type", "integer"); // 定义字段类型为整数
        fieldSetting.put("doc_values", true); // 启用doc values用于排序和聚合
        fieldSetting.put("store", false); // 不存储原始字段值
        return fieldSetting;
    }

    /**
     * format: 定义支持的日期格式，允许灵活处理输入。
     * doc_values: 默认启用，对排序和聚合非常有用。
     */
    private JSONObject getDateSetting() {
        JSONObject fieldSetting = new JSONObject();
        fieldSetting.put("type", "date"); // 定义字段类型为日期
        fieldSetting.put("format", "yyyy-MM-dd HH:mm:ss||epoch_millis"); // 支持多种日期格式
        fieldSetting.put("doc_values", true); // 启用doc values用于排序和聚合
        fieldSetting.put("store", false); // 不存储原始字段值
        return fieldSetting;
    }

    /**
     * ignore_above: 超过指定长度的字符串将不被索引。
     * doc_values: 默认启用，对排序和聚合非常有用。
     * index: 控制是否对该字段建立索引。
     * normalizer: 对于需要进行大小写转换的关键字字段很有用。
     */
    private JSONObject getKeywordSetting() {
        JSONObject fieldSetting = new JSONObject();
        fieldSetting.put("type", "keyword"); // 定义字段类型为关键字
        fieldSetting.put("ignore_above", 256); // 超过256字符的值将不会被索引
        fieldSetting.put("doc_values", true); // 启用doc values用于排序和聚合
        fieldSetting.put("index", true); // 是否对此字段建立索引
        fieldSetting.put("store", false); // 不存储原始字段值
        fieldSetting.put("normalizer", "lowercase"); // 使用小写转换的normalizer
        return fieldSetting;
    }

    /**
     * analyzer & search_analyzer: 默认使用standard分析器，适用于大多数语言的文本。
     * store: 如果需要直接从索引中获取字段值，则设为true。
     * term_vector: with_positions_offsets支持更高效的高亮显示等操作。
     * norms: 默认开启，对于不需要评分的字段可以关闭以节省空间。
     * fields: 添加一个keyword子字段以便于精确匹配或聚合。
     */
    private JSONObject getTextSetting() {
        JSONObject fieldSetting = new JSONObject();
        fieldSetting.put("type", "text"); // 定义字段类型为文本
        fieldSetting.put("analyzer", "standard"); // 使用标准分析器进行索引
        fieldSetting.put("search_analyzer", "standard"); // 搜索时使用标准分析器
        fieldSetting.put("store", false); // 不存储原始字段值
        fieldSetting.put("term_vector", "with_positions_offsets"); // 支持快速高亮显示和其他特性
        fieldSetting.put("norms", true); // 包含标准化数据（影响评分）
        fieldSetting.put("fields", new JSONObject().put("keyword", new JSONObject()
                .fluentPut("type", "keyword")
                .fluentPut("ignore_above", 256))); // 添加一个子字段用于精确匹配或聚合
        return fieldSetting;
    }
}
