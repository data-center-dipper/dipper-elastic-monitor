package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.elastic.slowsearch.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryPageReq;

import java.util.List;

public interface KillTimeoutRecordMapper {

    int insert(KillTimeoutRecord record);

    List<KillTimeoutRecord> queryPage(SlowQueryPageReq req, int offset, int limit);

    long count(SlowQueryPageReq req);

    KillTimeoutRecord getById(Integer id);
}