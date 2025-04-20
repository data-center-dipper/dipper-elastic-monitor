package com.dipper.monitor.service.elastic.dic.impl;

import com.dipper.monitor.entity.elastic.dic.Dic;
import com.dipper.monitor.mapper.DicMapper;
import com.dipper.monitor.service.elastic.dic.DicService;
import com.dipper.monitor.service.elastic.dic.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DicServiceImpl implements DicService {

    @Autowired
    private DicMapper dicMapper;
    @Autowired
    private WordService wordService;

    @Override
    public Dic addDic(Dic dic) { return dicMapper.insertDic(dic) > 0 ? dic : null; }
    @Override
    public Dic updateDic(Dic dic) { dicMapper.updateDic(dic); return dic; }

    @Override
    public void deleteDic(Integer dicId) {
        dicMapper.deleteDicById(dicId);
        wordService.deleteWordsByDicId(dicId);
    }

    @Override
    public Dic getDic(Integer id) { return dicMapper.getDicById(id); }
    @Override
    public List<Dic> getAllDics() { return dicMapper.getAllDics(); }
}