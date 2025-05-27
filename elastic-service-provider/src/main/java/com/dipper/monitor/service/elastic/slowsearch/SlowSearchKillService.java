package com.dipper.monitor.service.elastic.slowsearch;

import com.dipper.monitor.entity.elastic.slowsearch.kill.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillQueryReq;
import com.dipper.monitor.utils.Tuple2;

import java.util.List;

public interface SlowSearchKillService {
    /**
     * 终止正在执行的查询
     * @param killQueryReq 终止请求
     * @return 是否成功提交终止请求
     */
    boolean killQuery(KillQueryReq killQueryReq);

    /**
     * 分页查询终止记录
     * @param killPageReq 分页请求
     * @return 终止记录列表和总数
     */
    Tuple2<List<KillTimeoutRecord>, Long> killPage(KillPageReq killPageReq);
    
    /**
     * 获取终止记录详情
     * @param recordId 记录ID
     * @return 终止记录详情
     */
    KillTimeoutRecord getKillRecordDetail(Integer recordId);
}
