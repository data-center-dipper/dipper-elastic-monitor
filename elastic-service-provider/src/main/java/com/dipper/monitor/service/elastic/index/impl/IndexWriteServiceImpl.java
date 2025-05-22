package com.dipper.monitor.service.elastic.index.impl;

import com.dipper.monitor.entity.db.elastic.IndexWriteEntity;
import com.dipper.monitor.mapper.IndexWriteEntityMapper;
import com.dipper.monitor.service.elastic.index.IndexWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class IndexWriteServiceImpl implements IndexWriteService {

    @Autowired
    private IndexWriteEntityMapper indexWriteEntityMapper;

    @Override
    public void saveSingle(IndexWriteEntity entity) {
        indexWriteEntityMapper.insert(entity);
    }

    @Override
    public void saveBatch(List<IndexWriteEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return; // 防止空列表插入
        }
        indexWriteEntityMapper.batchInsert(entities);
    }

    @Override
    public List<IndexWriteEntity> getAll() {
        return indexWriteEntityMapper.selectAll();
    }

    @Override
    public int deleteByIndexName(String indexName) {
        return indexWriteEntityMapper.deleteByIndexName(indexName);
    }

    @Override
    public int batchDeleteByIndexNames(List<String> indexNames) {
        return indexWriteEntityMapper.batchDeleteByIndexNames(indexNames);
    }

    @Override
    public int deleteAll() {
        return indexWriteEntityMapper.deleteAll();
    }

}
