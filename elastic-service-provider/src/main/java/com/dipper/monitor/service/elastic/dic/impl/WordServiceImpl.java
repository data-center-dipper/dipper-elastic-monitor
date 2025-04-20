package com.dipper.monitor.service.elastic.dic.impl;

import com.dipper.monitor.entity.elastic.dic.Field;
import com.dipper.monitor.mapper.FieldMapper;
import com.dipper.monitor.service.elastic.dic.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private FieldMapper fieldMapper;

    @Override
    public Field addField(Field field) {
        int result = fieldMapper.insertField(field);
        return result > 0 ? field : null;
    }

    @Override
    public Field updateField(Field field) {
        int result = fieldMapper.updateField(field);
        return result > 0 ? field : null;
    }

    @Override
    public void deleteField(Integer id) {
        fieldMapper.deleteFieldById(id);
    }

    @Override
    public Field getField(Integer id) {
        return fieldMapper.getFieldById(id);
    }

    @Override
    public List<Field> getFieldsByDicId(Integer dicId) {
        return fieldMapper.getFieldsByDicId(dicId);
    }

    @Override
    public void deleteWordsByDicId(Integer dicId) {
        fieldMapper.deleteWordsByDicId(dicId);
    }
}
