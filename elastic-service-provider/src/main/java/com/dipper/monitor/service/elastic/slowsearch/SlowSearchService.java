package com.dipper.monitor.service.elastic.slowsearch;

import com.dipper.monitor.entity.elastic.slowsearch.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryView;
import com.dipper.monitor.utils.Tuple2;

import java.util.List;

public interface SlowSearchService {
    /**
     * 分页查询慢查询列表
     * @param pageReq 分页请求参数
     * @return 慢查询列表和总数
     */
    Tuple2<List<SlowQueryView>, Long> queryPage(SlowQueryPageReq pageReq);
    
    /**
     * 获取慢查询详情
     * @param queryId 查询ID
     * @return 慢查询详情
     */
    SlowQueryView getQueryDetail(Integer queryId);
    
    /**
     * 终止正在执行的查询
     * @param queryId 查询ID
     * @return 是否成功终止
     */
    boolean killQuery(Integer queryId);
    
    /**
     * 刷新慢查询列表
     * @return 最新的慢查询列表
     */
    List<SlowQueryView> refreshQueryList();
    
    /**
     * 记录查询终止超时
     * @param queryId 查询ID
     * @param reason 超时原因
     */
    void recordKillTimeout(Integer queryId, String reason);
    
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
