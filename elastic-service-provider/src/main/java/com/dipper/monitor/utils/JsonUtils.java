package com.dipper.monitor.utils;

import java.util.Map;


import com.alibaba.fastjson.JSON;
import java.util.Map;

public class JsonUtils {
    public static String toJson(Map<String, Object> map) {
        return JSON.toJSONString(map);
    }
}