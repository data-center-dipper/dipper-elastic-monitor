package com.dipper.monitor.service.elastic.index;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

public interface IndexOneOperatorService {
    String openIndexs(String index) throws IOException;

    boolean closeOneIndex(String index);

    boolean deleteIndex(String index);
}
