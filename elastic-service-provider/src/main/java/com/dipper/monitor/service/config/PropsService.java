package com.dipper.monitor.service.config;

import com.dipper.monitor.entity.db.config.ConfItemEntity;

import java.util.List;

public interface PropsService {

    void addIfNotExist(ConfItemEntity item);

    void save(ConfItemEntity item);

    void update(ConfItemEntity item);

    Long getValueByKeyToLong(String s, Long aLong);

    ConfItemEntity getConfigItemByKey(String key);

    String getConfigByKey(String key);

    Integer getValueByKeyToInt(String s);

    void saveOrUpdate(ConfItemEntity confItemEntity);

    void saveOrUpdate(String s, String s1);

    List<ConfItemEntity> getConfigList();

    <T> T getConfigToObject(String key, Class<T> clazz)  ;

    int getValueByKeyInteger(String diskKey, int i);

    String getValueByKeyString(String s, String max);
}
