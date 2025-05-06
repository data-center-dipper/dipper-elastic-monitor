package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.db.config.ConfItemEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommonPropsMapper {
    ConfItemEntity getConfByKey(@Param("configKey") String configKey);
    void save(ConfItemEntity confItemEntity);
    void update(ConfItemEntity confItemEntity);
    void deleteByKey(@Param("configKey") String configKey);

    List<ConfItemEntity> getConfigList();
}
