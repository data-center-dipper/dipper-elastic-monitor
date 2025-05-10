package com.dipper.monitor.service.elastic.dic.impl;

import com.dipper.monitor.entity.elastic.dic.Dic;
import com.dipper.monitor.entity.elastic.dic.DicPageInfo;
import com.dipper.monitor.mapper.DicMapper;
import com.dipper.monitor.service.elastic.dic.DicService;
import com.dipper.monitor.service.elastic.dic.WordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DicServiceImpl implements DicService {

    @Autowired
    private DicMapper dicMapper;
    @Autowired
    @Lazy
    private WordService wordService;

    @Override
    public Dic addDic(Dic dic) {
        check(dic);
        existDicByName(dic.getEnName());
        return dicMapper.insertDic(dic) > 0 ? dic : null;
    }

    @Override
    public Dic updateDic(Dic dic) {
        check(dic);
        return dicMapper.updateDic(dic) > 0 ? dic : null;
    }

    @Override
    public void deleteDic(Integer dicId) {
        if (dicId == null || dicId <= 0) {
            throw new IllegalArgumentException("Invalid dicId: " + dicId + ". dicId must be a positive integer.");
        }
        dicMapper.deleteDicById(dicId);
        wordService.deleteWordsByDicId(dicId);
    }

    @Override
    public Dic getDic(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid id: " + id + ". ID must be a positive integer.");
        }
        return dicMapper.getDicById(id);
    }

    @Override
    public List<Dic> getAllDics() {
        return dicMapper.getAllDics();
    }

    @Override
    public boolean existDic(Integer dicId) {
        if (dicId == null || dicId <= 0) {
            throw new IllegalArgumentException("Invalid dicId: " + dicId + ". dicId must be a positive integer.");
        }
        Dic dicById = dicMapper.getDicById(dicId);
        return dicById != null;
    }

    @Override
    public boolean existDicByName(String dicName) {
        if (dicName == null || dicName.trim().isEmpty()) {
            throw new IllegalArgumentException("Dictionary name cannot be null or empty.");
        }
        Dic dicByName = dicMapper.getDicByName(dicName);
        return dicByName != null;
    }

    @Override
    public Dic getDicByName(String dicName) {
        if (dicName == null || dicName.trim().isEmpty()) {
            throw new IllegalArgumentException("Dictionary name cannot be null or empty.");
        }
        Dic dicByName = dicMapper.getDicByName(dicName);
        return dicByName;
    }

    @Override
    public Integer getDicNum(DicPageInfo dicPageInfo) {
        String keyword = dicPageInfo.getKeyword();
        return dicMapper.getDicNum(keyword);
    }

    @Override
    public List<Dic> getDicByPage(DicPageInfo dicPageInfo) {
        Integer pageNum = dicPageInfo.getPageNum();
        if(pageNum == null){
            pageNum = 0;
        }else {
            pageNum = pageNum - 1;
        }
        dicPageInfo.setPageNum(pageNum);
        return dicMapper.getDicByPage(dicPageInfo);
    }

    @Override
    public Integer getDicIdByName(String dicName) {
        Integer dicId = null;
        if(StringUtils.isNotBlank(dicName)){
            Dic dic = getDicByName(dicName);
            if(dic != null){
                dicId = dic.getId();
            }
        }
        return dicId;
    }

    @Override
    public Map<String,Dic> getAllDicMap() {
        List<Dic> allDics = getAllDics();
        Map<String, Dic> collect = allDics.stream().collect(Collectors.toMap(Dic::getEnName, dic -> dic));
        return collect;
    }

    @Override
    public Map<Integer, Dic> getAllDicIdMap() {
        List<Dic> allDics = getAllDics();
        Map<Integer, Dic> collect = allDics.stream().collect(Collectors.toMap(Dic::getId, dic -> dic));
        return collect;
    }

    private void check(Dic dic) {
        if (dic == null) {
            throw new IllegalArgumentException("Dic object cannot be null.");
        }
        if (dic.getZhName() == null || dic.getZhName().trim().isEmpty()) {
            throw new IllegalArgumentException("Chinese name cannot be null or empty.");
        }
        if (dic.getEnName() == null || dic.getEnName().trim().isEmpty()) {
            throw new IllegalArgumentException("English name cannot be null or empty.");
        }
        // 可选：进一步验证 businessAttribute 的有效性（如果需要）
    }
}