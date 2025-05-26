package com.dipper.monitor.service.elastic.slowsearch.impl;

import com.dipper.monitor.entity.elastic.slowsearch.kill.KillQueryResult;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryKillStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SlowQueryKillStoreServiceImpl implements SlowQueryKillStoreService {
    @Override
    public void saveKillRecord(KillQueryResult result) {

    }
}
