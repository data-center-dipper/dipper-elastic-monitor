package com.dipper.monitor.service.elastic.slowsearch;

import com.dipper.monitor.entity.elastic.slowsearch.kill.KillQueryResult;

public interface SlowQueryKillStoreService {
    void saveKillRecord(KillQueryResult result);
}
