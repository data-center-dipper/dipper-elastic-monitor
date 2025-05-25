package com.dipper.monitor.entity.elastic.thread.check.realtime;

import lombok.Data;

@Data
public class ThreadCheckItem {
    private String category;
    private String item;
    private String value;
    private String threshold;
    private String status;
    private String description;
}