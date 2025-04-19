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
}
