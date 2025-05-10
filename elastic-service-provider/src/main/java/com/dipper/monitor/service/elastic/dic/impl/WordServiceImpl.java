package com.dipper.monitor.service.elastic.dic.impl;

import com.dipper.monitor.entity.elastic.dic.*;
import com.dipper.monitor.mapper.FieldMapper;
import com.dipper.monitor.service.elastic.dic.DicService;
import com.dipper.monitor.service.elastic.dic.WordService;
import com.dipper.monitor.utils.elastic.ElasticFieldMapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private FieldMapper fieldMapper;
    @Autowired
    private DicService dicService;

    @Override
    public Field addField(Field field) {
        checkField(field);
        Integer dicId = field.getDicId();
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

        existField(dicId,field.getEnName());

        int result = fieldMapper.insertField(field);
        return result > 0 ? field : null;
    }

    @Override
    public void addFields(List<Field> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Fields list cannot be null or empty.");
        }

        Integer dicId = null;
        for (Field field : fields) {
            checkField(field);
            if (dicId == null) {
                dicId = field.getDicId();
            } else if (!dicId.equals(field.getDicId())) {
                throw new IllegalArgumentException("All fields must belong to the same dictionary.");
            }
        }

        validateDicId(dicId);

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

            existField(dicId,field.getEnName());
            fieldMapper.insertField(field);
        }
    }

    @Override
    public Integer getWordNum(WordPageInfo wordPageInfo) {
        String keyword = wordPageInfo.getKeyword();
        String dicName = wordPageInfo.getDicName();
        Integer dicId = dicService.getDicIdByName(dicName);
        return fieldMapper.getWordNum(keyword,dicId);
    }

    @Override
    public List<WodListView> getWordByPage(WordPageInfo wordPageInfo) {
        // 设置默认页码为0（如果未提供）
        Integer pageNum = wordPageInfo.getPageNum();
        if(pageNum == null || pageNum < 1){
            pageNum = 1;
        }

        // 计算offset
        Integer offset = (pageNum - 1) * wordPageInfo.getPageSize();

        // 获取字典ID通过字典名称
        Integer dicId = dicService.getDicIdByName(wordPageInfo.getDicName());

        // 创建WordPageSearch对象并复制WordPageInfo中的属性
        WordPageSearch wordPageSearch = new WordPageSearch();
        BeanUtils.copyProperties(wordPageInfo, wordPageSearch);
        wordPageSearch.setDicId(dicId); // 设置字典ID
        wordPageSearch.setOffset(offset); // 设置偏移量

        // 查询数据库获取分页后的词列表
        List<Field> wordByPage = fieldMapper.getWordByPage(wordPageSearch);

        // 获取所有字典的映射
        Map<Integer, Dic> allDicMap = dicService.getAllDicIdMap();

        // 转换为WodListView列表
        List<WodListView> wodListViewList = new ArrayList<>();
        for (Field field : wordByPage) {
            WodListView wodListView = new WodListView();
            BeanUtils.copyProperties(field, wodListView);

            Dic dic = allDicMap.get(field.getDicId());
            if(dic != null){
                wodListView.setDicZhName(dic.getZhName());
                wodListView.setDicEnName(dic.getEnName());
            }

            wodListViewList.add(wodListView);
        }
        return wodListViewList;
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
    private void existField(Integer dicId,String enName) {
        if (StringUtils.isBlank(enName)) {
            throw new IllegalArgumentException("Field English name cannot be null or empty.");
        }
        Field existingField = fieldMapper.getFieldByDicIdAndEnName(dicId,enName);
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