package com.dipper.monitor.service.elastic.slowsearch.impl;

import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQueryPageReq;
import com.dipper.monitor.mapper.SlowQueryMapper;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SlowQueryStoreServiceImpl implements SlowQueryStoreService {

    @Autowired
    private SlowQueryMapper slowQueryMapper;


    // 每次批量插入的最大数量
    private static final int BATCH_SIZE = 500;


    /**
     * 批量保存慢查询记录
     *
     * @param slowQueries 要保存的慢查询列表
     */
    @Override
    public void saveSlowQueries(List<SlowQueryEntity> slowQueries) {
        if (slowQueries == null || slowQueries.isEmpty()) {
            log.warn("传入的慢查询列表为空，跳过保存");
            return;
        }

        log.info("开始批量插入 {} 条慢查询日志", slowQueries.size());
        int rowsAffected = slowQueryMapper.saveSlowQueries(slowQueries);
        log.info("慢查询日志插入完成");
    }

    @Override
    public void cleanHistoryData(int retentionDays) {
        slowQueryMapper.cleanHistoryData(retentionDays);
    }

    @Override
    public int queryPageNum(SlowQueryPageReq pageReq) {
        return slowQueryMapper.queryPageNum(pageReq);
    }

    @Override
    public List<SlowQueryEntity> queryPage(SlowQueryPageReq pageReq) {
        int offset = (pageReq.getPageNum() - 1) * pageReq.getPageSize();
        pageReq.setOffset(offset);

        List<SlowQueryEntity> list = slowQueryMapper.queryPage(pageReq);
        return list;
    }

    @Override
    public SlowQueryEntity getQueryDetail(Integer queryId) {
        SlowQueryEntity slowQueryEntity = slowQueryMapper.selectById(queryId);
        return slowQueryEntity;
    }

    @Override
    public void updateSlowQuery(SlowQueryEntity entity) {
        slowQueryMapper.updateSlowQuery(entity);
    }

    @Override
    public List<SlowQueryEntity> queryByTimeRange(Date startTime, Date endTime) {
        try {
            // 创建查询参数
            SlowQueryPageReq pageReq = new SlowQueryPageReq();
            pageReq.setStartTime(startTime);
            pageReq.setEndTime(endTime);
            pageReq.setPageNum(1);
            pageReq.setPageSize(1000); // 设置一个较大的值以获取所有记录
            
            // 调用现有的分页查询方法
            List<SlowQueryEntity> list = slowQueryMapper.queryPage(pageReq);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            log.error("按时间范围查询慢查询记录异常: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

}
