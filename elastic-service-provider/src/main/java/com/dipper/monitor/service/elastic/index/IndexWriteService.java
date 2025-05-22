package com.dipper.monitor.service.elastic.index;

import com.dipper.monitor.entity.db.elastic.IndexWriteEntity;

import java.util.List;

public interface IndexWriteService {
    void saveSingle(IndexWriteEntity entity);

    void saveBatch(List<IndexWriteEntity> entities);

    List<IndexWriteEntity> getAll();

    int deleteByIndexName(String indexName);

    int batchDeleteByIndexNames(List<String> indexNames);

    int deleteAll();
}