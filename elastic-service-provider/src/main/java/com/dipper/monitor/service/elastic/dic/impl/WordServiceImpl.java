package com.dipper.monitor.service.elastic.dic.impl;

import com.dipper.monitor.entity.elastic.dic.Dic;
import com.dipper.monitor.entity.elastic.dic.Field;
import com.dipper.monitor.entity.elastic.dic.WordPageInfo;
import com.dipper.monitor.mapper.FieldMapper;
import com.dipper.monitor.service.elastic.dic.DicService;
import com.dipper.monitor.service.elastic.dic.WordService;
import com.dipper.monitor.utils.elastic.ElasticFieldMapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private FieldMapper fieldMapper;
    @Autowired
    private DicService dicService;

    @Override
    public Field addField(Field field) {
        checkField(field);
        validateDicId(field.getDicId());

        String fieldType = field.getFieldType();
        ElasticFieldMapUtils.checkFieldType(fieldType);

        String esMappingType = field.getEsMappingType();
        if (StringUtils.isBlank(esMappingType)) {
            esMappingType = ElasticFieldMapUtils.autoEsTypeMap(fieldType);
        } else {
            ElasticFieldMapUtils.checkEsFiledType(esMappingType);
        }
        field.setEsMappingType(esMappingType);

        existField(field.getEnName());

        int result = fieldMapper.insertField(field);
        return result > 0 ? field : null;
    }

    @Override
    public void addFields(List<Field> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Fields list cannot be null or empty.");
        }

        Integer commonDicId = null;
        for (Field field : fields) {
            checkField(field);
            if (commonDicId == null) {
                commonDicId = field.getDicId();
            } else if (!commonDicId.equals(field.getDicId())) {
                throw new IllegalArgumentException("All fields must belong to the same dictionary.");
            }
        }

        validateDicId(commonDicId);

        for (Field field : fields) {
            String fieldType = field.getFieldType();
            ElasticFieldMapUtils.checkFieldType(fieldType);

            String esMappingType = field.getEsMappingType();
            if (StringUtils.isBlank(esMappingType)) {
                esMappingType = ElasticFieldMapUtils.autoEsTypeMap(fieldType);
            } else {
                ElasticFieldMapUtils.checkEsFiledType(esMappingType);
            }
            field.setEsMappingType(esMappingType);

            existField(field.getEnName());
            fieldMapper.insertField(field);
        }
    }

    @Override
    public Integer getWordNum(WordPageInfo wordPageInfo) {
        String keyword = wordPageInfo.getKeyword();
        return fieldMapper.getWordNum(keyword);
    }

    @Override
    public List<Dic> getWordByPage(WordPageInfo wordPageInfo) {
        Integer pageNum = wordPageInfo.getPageNum();
        if(pageNum == null){
            pageNum = 0;
        }else {
            pageNum = pageNum - 1;
        }
        wordPageInfo.setPageNum(pageNum);
        return fieldMapper.getWordByPage(wordPageInfo);
    }

    private void checkField(Field field) {
        if (field == null) {
            throw new IllegalArgumentException("Field object cannot be null.");
        }
        if (StringUtils.isBlank(field.getZhName())) {
            throw new IllegalArgumentException("Chinese name cannot be null or empty.");
        }
        if (StringUtils.isBlank(field.getEnName())) {
            throw new IllegalArgumentException("English name cannot be null or empty.");
        }
        if (StringUtils.isBlank(field.getFieldType())) {
            throw new IllegalArgumentException("Field type cannot be null or empty.");
        }
        if (field.getDicId() == null || field.getDicId() <= 0) {
            throw new IllegalArgumentException("Dictionary ID must be a positive integer.");
        }
    }

    private void validateDicId(Integer dicId) {
        if (dicId == null || dicId <= 0 || !dicService.existDic(dicId)) {
            throw new RuntimeException("Invalid or non-existent dictionary ID: " + dicId);
        }
    }

    /**
     * 根据英文名称判断字段名是否存在
     * @param enName 英文名称
     */
    private void existField(String enName) {
        if (StringUtils.isBlank(enName)) {
            throw new IllegalArgumentException("Field English name cannot be null or empty.");
        }
        Field existingField = fieldMapper.getFieldByEnName(enName);
        if (existingField != null) {
            throw new RuntimeException("Field with this English name already exists: " + enName);
        }
    }

    @Override
    public Field updateField(Field field) {
        checkField(field);
        int result = fieldMapper.updateField(field);
        return result > 0 ? field : null;
    }

    @Override
    public void deleteField(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid field ID: " + id + ". Field ID must be a positive integer.");
        }
        fieldMapper.deleteFieldById(id);
    }

    @Override
    public Field getField(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid field ID: " + id + ". Field ID must be a positive integer.");
        }
        return fieldMapper.getFieldById(id);
    }

    @Override
    public List<Field> getFieldsByDicId(Integer dicId) {
        if (dicId == null || dicId <= 0) {
            throw new IllegalArgumentException("Invalid dictionary ID: " + dicId + ". Dictionary ID must be a positive integer.");
        }
        return fieldMapper.getFieldsByDicId(dicId);
    }

    @Override
    public void deleteWordsByDicId(Integer dicId) {
        if (dicId == null || dicId <= 0) {
            throw new IllegalArgumentException("Invalid dictionary ID: " + dicId + ". Dictionary ID must be a positive integer.");
        }
        fieldMapper.deleteWordsByDicId(dicId);
    }
}