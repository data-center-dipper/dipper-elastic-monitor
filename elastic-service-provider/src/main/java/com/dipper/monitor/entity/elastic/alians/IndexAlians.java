package com.dipper.monitor.entity.elastic.alians;

import lombok.Data;

@Data
public class IndexAlians {
    private String alias;
    private String index;
    private String filter;
    private String routingIndex;
    private String routingSearch;
    private Boolean isWriteIndex;

    public IndexAlians(String alias, String index, String filter, String routingIndex, String routingSearch) {
        this.alias = alias;
        this.index = index;
        this.filter = filter;
        this.routingIndex = routingIndex;
        this.routingSearch = routingSearch;
    }
}
