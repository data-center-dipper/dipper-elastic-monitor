package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.elastic.dic.Dic;
import com.dipper.monitor.entity.elastic.dic.DicPageInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DicMapper {
    int insertDic(Dic dic);
    int updateDic(Dic dic);
    int deleteDicById(Integer id);
    Dic getDicById(Integer id);
    List<Dic> getAllDics();

    Dic getDicByName(@Param("enName") String enName);

    List<Dic> getDicByPage(DicPageInfo dicPageInfo);

    Integer getDicNum(@Param("keyword") String keyword);

}