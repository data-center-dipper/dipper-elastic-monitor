package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.elastic.dic.Field;

import java.util.List;

public interface FieldMapper {
    int insertField(Field field);
    int updateField(Field field);
    int deleteFieldById(Integer id);
    Field getFieldById(Integer id);
    List<Field> getFieldsByDicId(Integer dicId);

    void deleteWordsByDicId(Integer dicId);
}