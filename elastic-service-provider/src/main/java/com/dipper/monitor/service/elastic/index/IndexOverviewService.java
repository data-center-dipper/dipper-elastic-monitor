package com.dipper.monitor.service.elastic.index;

import com.dipper.monitor.entity.db.elastic.IndexWriteEntity;

import java.util.List;

public interface IndexOverviewService {
    /**
     * 获取正在写入的索引信息
     * @return
     */
    List<IndexWriteEntity> writeIndexList();

    /**
     * 获取未来索引列表
     * @return
     */
    List<IndexWriteEntity> featureIndexList();
}
