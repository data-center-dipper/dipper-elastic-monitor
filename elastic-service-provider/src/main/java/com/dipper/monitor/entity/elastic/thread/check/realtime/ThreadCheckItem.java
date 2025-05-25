package com.dipper.monitor.entity.elastic.thread.check.realtime;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ThreadCheckItem {
    private String name;
    private String category;
    private String item;
    private String value;
    private String threshold;
    private String status;
    private String description;
}