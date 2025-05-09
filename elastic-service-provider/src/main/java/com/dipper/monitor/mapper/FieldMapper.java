package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.elastic.dic.Dic;
import com.dipper.monitor.entity.elastic.dic.Field;
import com.dipper.monitor.entity.elastic.dic.WordPageInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FieldMapper {

    /**
     * 插入字段信息
     * @param field 字段对象
     * @return 影响的行数
     */
    int insertField(Field field);

    /**
     * 更新字段信息
     * @param field 字段对象
     * @return 影响的行数
     */
    int updateField(@Param("field") Field field);

    /**
     * 根据ID删除字段
     * @param id 字段ID
     * @return 影响的行数
     */
    int deleteFieldById(@Param("id") Integer id);

    /**
     * 根据ID获取字段信息
     * @param id 字段ID
     * @return 字段对象
     */
    Field getFieldById(@Param("id") Integer id);

    /**
     * 根据字典ID获取字段列表
     * @param dicId 字典ID
     * @return 字段列表
     */
    List<Field> getFieldsByDicId(@Param("dicId") Integer dicId);

    /**
     * 根据字典ID删除相关词汇
     * @param dicId 字典ID
     */
    void deleteWordsByDicId(@Param("dicId") Integer dicId);

    /**
     * 根据英文名称获取字段信息
     * @param enName 英文名称
     * @return 字段对象
     */
    Field getFieldByEnName(@Param("enName") String enName);

    /**
     * 根据英文名称判断字段是否存在
     * @param keyword
     * @return
     */
    Integer getWordNum(String keyword);

    /**
     * 获取分页字段信息
     * @param wordPageInfo
     * @return
     */
    List<Dic> getWordByPage(WordPageInfo wordPageInfo);
}