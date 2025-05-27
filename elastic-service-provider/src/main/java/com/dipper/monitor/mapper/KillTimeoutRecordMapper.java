package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.elastic.slowsearch.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillPageReq;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface KillTimeoutRecordMapper {

    int insert(KillTimeoutRecord record);

    List<KillTimeoutRecord> queryPage( @RequestParam("killPageReq")  KillPageReq killPageReq,
                                      @RequestParam("offset") int offset,
                                      @RequestParam("limit")  int limit);

    long count(KillPageReq killPageReq);

    KillTimeoutRecord getById(Integer id);
}