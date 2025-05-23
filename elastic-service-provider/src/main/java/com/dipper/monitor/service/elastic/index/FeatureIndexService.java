package com.dipper.monitor.service.elastic.index;

import com.dipper.monitor.entity.db.elastic.EsTemplateEntity;
import com.dipper.monitor.entity.db.elastic.IndexWriteEntity;

import java.util.List;

public interface FeatureIndexService {
    List<IndexWriteEntity> featureIndexList();

    public List<IndexWriteEntity> getFeatureIndexList(EsTemplateEntity entity);
}
