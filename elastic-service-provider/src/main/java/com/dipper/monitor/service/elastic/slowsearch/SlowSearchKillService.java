package com.dipper.monitor.service.elastic.slowsearch;

import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryView;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillQueryReq;

import java.util.List;

public interface SlowSearchKillService {
    /**
     * 终止正在执行的查询
     * @return 是否成功终止
     */
    boolean killQuery(KillQueryReq killQueryReq);
}
