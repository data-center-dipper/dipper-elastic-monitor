package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.elastic.dic.Dic;

import java.util.List;

public interface DicMapper {
    int insertDic(Dic dic);
    int updateDic(Dic dic);
    int deleteDicById(Integer id);
    Dic getDicById(Integer id);
    List<Dic> getAllDics();

    Dic getDicByName(String enName);
}