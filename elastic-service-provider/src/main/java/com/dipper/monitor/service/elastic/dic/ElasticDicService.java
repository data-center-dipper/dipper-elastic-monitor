package com.dipper.monitor.service.elastic.dic;

import com.alibaba.fastjson.JSONObject;

public interface ElasticDicService {
    JSONObject getElasticMapping(String dicName);
}
