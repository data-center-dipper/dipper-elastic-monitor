package com.dipper.monitor.entity.elastic.segments;

import lombok.Data;

@Data
public class SegmentMessage {
    private String index;
    private Integer shard;
    private String prirep;
    private String ip;
    private String segment;
    private Long generation;
    private Long docsCount;
    private Long docsDeleted;
    private String size;
    private Long sizeMemory;
    private Boolean committed;
    private Boolean searchable;
    private String version;
    private Boolean compound;

    public SegmentMessage(String index, Integer shard, String prirep, String ip, String segment, Long generation, Long docsCount, Long docsDeleted, String size, Long sizeMemory, Boolean committed, Boolean searchable, String version, Boolean compound) {
        this.index = index;
        this.shard = shard;
        this.prirep = prirep;
        this.ip = ip;
        this.segment = segment;
        this.generation = generation;
        this.docsCount = docsCount;
        this.docsDeleted = docsDeleted;
        this.size = size;
        this.sizeMemory = sizeMemory;
        this.committed = committed;
        this.searchable = searchable;
        this.version = version;
        this.compound = compound;
    }
}
