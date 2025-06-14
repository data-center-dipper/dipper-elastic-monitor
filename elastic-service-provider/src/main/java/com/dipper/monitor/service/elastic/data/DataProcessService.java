package com.dipper.monitor.service.elastic.data;

import com.dipper.monitor.entity.elastic.data.ProgressInfo;

import java.io.IOException;

public interface DataProcessService {
    /**
     * 节点下线
     * @param nodeName
     * @return
     */
    void nodeOfflineApi(String nodeName) throws IOException;

    /**
     * 获取节点下线任务
     * @return
     */
    String nodeOfflineState();
}
