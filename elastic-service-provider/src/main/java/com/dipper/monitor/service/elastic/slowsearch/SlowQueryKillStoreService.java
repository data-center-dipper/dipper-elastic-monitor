package com.dipper.monitor.service.elastic.slowsearch;

import com.dipper.monitor.entity.elastic.slowsearch.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillQueryResult;
import com.dipper.monitor.utils.Tuple2;

import java.util.List;

public interface SlowQueryKillStoreService {
    /**
     * 保存终止记录
     * @param result 终止结果
     */
    void saveKillRecord(KillQueryResult result);
}
