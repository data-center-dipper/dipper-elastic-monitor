package com.dipper.monitor.service.elastic.dic;

import com.dipper.monitor.entity.elastic.dic.Field;

import java.util.List;

public interface WordService {

    Field addField(Field field);
    Field updateField(Field field);
    void deleteField(Integer id);
    Field getField(Integer id);
    List<Field> getFieldsByDicId(Integer dicId);

    void deleteWordsByDicId(Integer dicId);

    /**
     * 批量添加字段的方法
     * @param fields
     */
    void addFields(List<Field> fields);
}
