package com.dipper.monitor.service.config.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.config.ConfItemEntity;
//import com.dipper.monitor.myibatis.CommonPropsMapper;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.mapper.CommonPropsMapper;
import com.dipper.monitor.service.config.PropsService;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PropsServiceImpl implements PropsService {


    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private CommonPropsMapper commonPropsMapper;
    @Autowired
    private ElasticClusterManagerService elasticClusterManagerService;


    @Override
    public void addIfNotExist(ConfItemEntity item) {
        CurrentClusterEntity currentCluster = elasticClusterManagerService.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();

        item.validate();
        String configKey = item.getConfigKey();
        ConfItemEntity configByName = getConfigItemByKey(configKey);
        if (configByName != null) {
            return;
        }
        save(item);
    }



    @Override
    public void save(ConfItemEntity item) {
        CurrentClusterEntity currentCluster = elasticClusterManagerService.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        item.setClusterCode(clusterCode);
        commonPropsMapper.save(item);
    }

    @Override
    public void update(ConfItemEntity confItemEntity) {
        commonPropsMapper.update(confItemEntity);
    }

    @Override
    public ConfItemEntity getConfigItemByKey(String key) {
        ConfItemEntity confByName = getConfByKey(key);
        return confByName;
    }

    @Override
    public ConfItemEntity getConfigItemByEnum(ConfItemEntity item) {
        String moduleName = item.getModuleName();
        String entityName = item.getEntityName();
        String sectionName = item.getSectionName();
        String configKey = item.getConfigKey();

        CurrentClusterEntity currentCluster = elasticClusterManagerService.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();

        ConfItemEntity confByName = commonPropsMapper.getConfigItemByEnum(clusterCode,moduleName,entityName,
                sectionName,configKey);

        return confByName;
    }

    private ConfItemEntity getConfByKey(String key) {
        CurrentClusterEntity currentCluster = elasticClusterManagerService.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        ConfItemEntity confByName = commonPropsMapper.getConfByKey(clusterCode,key);
        return confByName;
    }


    @Override
    public String getConfigByKey(String key) {
        ConfItemEntity confByKey = getConfByKey(key);
        if (confByKey == null) {
            return null;
        }
        String configValue = confByKey.getConfigValue();
        return configValue;
    }

    @Override
    public Long getValueByKeyToLong(String key, Long aLong) {
        ConfItemEntity confByKey = getConfByKey(key);
        if (confByKey == null) {
            return aLong;
        }
        Long configValue = Long.parseLong(confByKey.getConfigValue());
        return configValue;
    }


    @Override
    public Integer getValueByKeyToInt(String key) {
        ConfItemEntity confByKey = getConfByKey(key);
        if (confByKey == null) {
            return null;
        }
        Integer configValue = Integer.parseInt(confByKey.getConfigValue());
        return configValue;
    }

    @Override
    public void saveOrUpdate(ConfItemEntity confItemEntity) {
        confItemEntity.validate();
        String configKey = confItemEntity.getConfigKey();
        ConfItemEntity configByKey = getConfigItemByKey(configKey);
        if (configByKey != null) {
            confItemEntity.setId(configByKey.getId());
            update(confItemEntity);
            return;
        }
        save(confItemEntity);
    }


    @Override
    public void saveOrUpdate(String key, String value) {
        ConfItemEntity confItemEntity = new ConfItemEntity();
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
        ConfItemEntity confByKey = getConfByKey(key);
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
