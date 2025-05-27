package com.dipper.monitor.service.elastic.slowsearch;

import com.dipper.monitor.entity.elastic.slowsearch.kill.KillQueryResult;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillTimeoutRecord;

import java.util.Date;
import java.util.List;

public interface SlowQueryKillStoreService {
    /**
     * 保存终止记录
     * @param result 终止结果
     */
    void saveKillRecord(KillQueryResult result);

    List<KillTimeoutRecord> queryByTimeRange(Date startTime, Date endTime);
}
