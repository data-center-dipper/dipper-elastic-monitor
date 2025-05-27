package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.elastic.slowsearch.kill.KillTimeoutRecord;
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

    /**
     * 根据时间范围查询终止记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 终止记录列表
     */
    List<KillTimeoutRecord> queryByTimeRange(@RequestParam("startTime") String startTime,
                                           @RequestParam("endTime") String endTime);
}