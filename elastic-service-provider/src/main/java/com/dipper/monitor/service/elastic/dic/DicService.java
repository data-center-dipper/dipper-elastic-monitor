package com.dipper.monitor.service.elastic.dic;

import com.dipper.monitor.entity.elastic.dic.Dic;

import java.util.List;

public interface DicService {
    Dic addDic(Dic dic);
    Dic updateDic(Dic dic);
    void deleteDic(Integer id);
    Dic getDic(Integer id);
    List<Dic> getAllDics();

    boolean existDic(Integer dicId);

    public boolean existDicByName(String dicName);
}