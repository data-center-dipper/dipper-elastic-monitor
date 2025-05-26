package com.dipper.monitor.service.elastic.slowsearch;

import com.dipper.monitor.entity.elastic.slowsearch.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryView;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.util.List;

public interface SlowSearchService {
    /**
     * 分页查询慢查询列表
     * @param pageReq 分页请求参数
     * @return 慢查询列表和总数
     */
    Tuple2<List<SlowQueryView>, Integer> slowSearchPage(SlowQueryPageReq pageReq) throws IOException;
    



    /**
     * 分页查询终止超时记录
     * @param pageReq 分页请求参数
     * @return 超时记录列表和总数
     */
    Tuple2<List<KillTimeoutRecord>, Long> queryKillTimeoutPage(SlowQueryPageReq pageReq);
    
    /**
     * 获取终止超时记录详情
     * @param recordId 记录ID
     * @return 超时记录详情
     */
    KillTimeoutRecord getKillTimeoutDetail(Integer recordId);

}
