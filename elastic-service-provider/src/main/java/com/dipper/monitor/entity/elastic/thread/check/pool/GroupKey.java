package com.dipper.monitor.entity.elastic.thread.check.pool;

import lombok.Data;

@Data
public class GroupKey {
    private String key;
    private String nodeName;
    private String name;

    public GroupKey(String key, String nodeName, String name) {
        this.key = key;
        this.nodeName = nodeName;
        this.name = name;
    }

    public String toString() {
        return key ;
    }
}
