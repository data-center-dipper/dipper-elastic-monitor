package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.db.elastic.IndexWriteEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IndexWriteEntityMapper {

    void insert(IndexWriteEntity entity);

    List<IndexWriteEntity> selectAll();

    int deleteByIndexName(String indexName);

    int batchDeleteByIndexNames(List<String> indexNames);

    int deleteAll();

    // 批量插入方法
    void batchInsert(List<IndexWriteEntity> entities);
}