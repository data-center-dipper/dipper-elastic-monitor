package com.dipper.monitor.entity.elastic.thread.hot;

import lombok.Data;

import java.util.Map;

@Data
public class HotThreadMeta {
    private String dateTime;
    public Map<String, String> maps;
    public HotThreadMeta(String dateTime, Map<String, String> maps) {
        this.dateTime = dateTime;
        this.maps = maps;
    }
}
