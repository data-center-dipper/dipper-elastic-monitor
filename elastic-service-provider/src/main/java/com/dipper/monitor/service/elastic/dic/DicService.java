package com.dipper.monitor.service.elastic.dic;

import com.dipper.monitor.entity.elastic.dic.Dic;
import com.dipper.monitor.entity.elastic.dic.DicPageInfo;

import java.util.List;

public interface DicService {
    Dic addDic(Dic dic);
    Dic updateDic(Dic dic);
    void deleteDic(Integer id);
    Dic getDic(Integer id);
    List<Dic> getAllDics();

    boolean existDic(Integer dicId);

    public boolean existDicByName(String dicName);

    Dic getDicByName(String dicName);

    /**
     * 获取字典数量
     * @return
     */
    Integer getDicNum(DicPageInfo dicPageInfo);

    /**
     * 分页获取字典
     * @param dicPageInfo
     * @return
     */
    List<Dic> getDicByPage(DicPageInfo dicPageInfo);
}