package com.dipper.monitor.service.elastic.slowsearch.impl;

import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.mapper.SlowQueryMapper;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        slowQueryMapper.deleteById()
    }

}
