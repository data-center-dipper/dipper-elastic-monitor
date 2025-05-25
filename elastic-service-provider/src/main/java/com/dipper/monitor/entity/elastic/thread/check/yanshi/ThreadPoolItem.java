package com.dipper.monitor.entity.elastic.thread.check.yanshi;

import lombok.Data;

@Data
public class ThreadPoolItem {
    private String nodeName;
    private String name;
    private Integer active;
    private Integer queue;
    private Integer rejected;
}