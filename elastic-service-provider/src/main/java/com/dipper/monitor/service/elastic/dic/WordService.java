package com.dipper.monitor.service.elastic.dic;

import com.dipper.monitor.entity.elastic.dic.*;

import java.util.List;

public interface WordService {

    Field addField(Field field);

    /**
     * 更新字段
     * @param updateFieldReq
     * @return
     */
    Field updateField(UpdateFieldReq updateFieldReq);
    void deleteField(Integer id);
    Field getField(Integer id);
    List<Field> getFieldsByDicId(Integer dicId);

    void deleteWordsByDicId(Integer dicId);

    /**
     * 批量添加字段的方法
     * @param fields
     */
    void addFields(List<Field> fields);

    /**
     * 根据分页信息获取字段列表
     * @param wordPageInfo
     * @return
     */
    Integer getWordNum(WordPageInfo wordPageInfo);

    /**
     * 根据分页信息获取字段列表
     * @param wordPageInfo
     * @return
     */
    List<WodListView> getWordByPage(WordPageInfo wordPageInfo);
}
