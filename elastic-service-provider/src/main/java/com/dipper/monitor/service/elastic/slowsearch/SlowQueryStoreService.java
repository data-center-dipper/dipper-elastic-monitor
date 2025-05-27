package com.dipper.monitor.service.elastic.slowsearch;

import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;

import java.util.Date;
import java.util.List;

import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQueryPageReq;

/**
 * 慢查询日志存储服务接口
 */
public interface SlowQueryStoreService {

    /**
     * 批量保存慢查询记录
     *
     * @param slowQueries 要保存的慢查询列表
     */
    void saveSlowQueries(List<SlowQueryEntity> slowQueries);

    void cleanHistoryData(int retentionDays);

    int queryPageNum(SlowQueryPageReq pageReq);

    List<SlowQueryEntity> queryPage(SlowQueryPageReq pageReq);

    SlowQueryEntity getQueryDetail(Integer queryId);

    void updateSlowQuery(SlowQueryEntity entity);

    List<SlowQueryEntity>  queryByTimeRange(Date startTime, Date endTime);
}