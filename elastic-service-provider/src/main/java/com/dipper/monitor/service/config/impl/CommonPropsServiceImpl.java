package com.dipper.monitor.service.config.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.config.ConfItemEntity;
//import com.dipper.monitor.myibatis.CommonPropsMapper;
import com.dipper.monitor.mapper.CommonPropsMapper;
import com.dipper.monitor.service.config.CommonPropsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CommonPropsServiceImpl implements CommonPropsService {


    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private CommonPropsMapper commonPropsMapper;


    @Override
    public void addIfNotExist(ConfItemEntity item) {
        item.validate();
        String configName = item.getConfigName(); // 修改为configName
        ConfItemEntity configByName = getConfigItemByKey(configName);
        if (configByName != null) {
            return;
        }
        save(item);
    }



    @Override
    public void save(ConfItemEntity item) {
        commonPropsMapper.save(item);
    }

    @Override
    public void update(ConfItemEntity confItemEntity) {
        commonPropsMapper.update(confItemEntity);
    }

    @Override
    public ConfItemEntity getConfigItemByKey(String key) {
        ConfItemEntity confByName = commonPropsMapper.getConfByKey(key); // 使用configName查询
        return confByName;
    }


    @Override
    public String getConfigByKey(String key) {
        ConfItemEntity confByKey = commonPropsMapper.getConfByKey(key);
        if (confByKey == null) {
            return null;
        }
        String configValue = confByKey.getConfigValue();
        return configValue;
    }

    @Override
    public Long getValueByKeyToLong(String key, Long aLong) {
        ConfItemEntity confByKey = commonPropsMapper.getConfByKey(key);
        if (confByKey == null) {
            return aLong;
        }
        Long configValue = Long.parseLong(confByKey.getConfigValue());
        return configValue;
    }


    @Override
    public Integer getValueByKeyToInt(String key) {
        ConfItemEntity confByKey = commonPropsMapper.getConfByKey(key);
        if (confByKey == null) {
            return null;
        }
        Integer configValue = Integer.parseInt(confByKey.getConfigValue());
        return configValue;
    }

    @Override
    public void saveOrUpdate(ConfItemEntity confItemEntity) {
        confItemEntity.validate();
        String configKey = confItemEntity.getConfigName();
        ConfItemEntity configByKey = getConfigItemByKey(configKey);
        if (configByKey != null) {
            update(confItemEntity);
            return;
        }
        save(confItemEntity);
    }


    @Override
    public void saveOrUpdate(String key, String value) {
        ConfItemEntity confItemEntity = new ConfItemEntity();
        confItemEntity.setDynamicData(false);
        confItemEntity.setCustomType(true);
        confItemEntity.setShowView(true);
        confItemEntity.setConfigType("string");
        confItemEntity.setConfigName(key);
        confItemEntity.setConfigValue(value);
        confItemEntity.setConfigName(key);
        saveOrUpdate(confItemEntity);
    }

    @Override
    public List<ConfItemEntity> getConfigList() {
        return commonPropsMapper.getConfigList();
    }

    @Override
    public <T> T getConfigToObject(String key, Class<T> clazz) {
        ConfItemEntity confByKey = commonPropsMapper.getConfByKey(key);
        if (confByKey == null) {
            return null;
        }
        T t = JSONObject.parseObject(confByKey.getConfigValue(), clazz);
        return t;
    }

    @Override
    public int getValueByKeyInteger(String diskKey, int i) {
        return 0;
    }

    @Override
    public String getValueByKeyString(String s, String max) {
        return "";
    }


}
