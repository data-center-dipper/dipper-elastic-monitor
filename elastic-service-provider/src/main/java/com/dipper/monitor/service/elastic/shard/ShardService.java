package com.dipper.monitor.service.elastic.shard;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.List;

public interface ShardService {
    /**
     * 获取分片异常列表
     * @return
     * @throws IOException
     */
    List<JSONObject> getShardError() throws IOException;

    /**
     * 分片异常检测
     * @return
     */
    String checkShardError() throws Exception;

    /**
     * 修复分片异常
     * @return
     */
    String repairShardError() throws Exception;


}
