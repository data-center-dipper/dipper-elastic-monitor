package com.dipper.monitor.entity.elastic.alians;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class IndexAlias {
    private String alias;
    private String index;
    private String filter;
    private String routingIndex;
    private String routingSearch;
    private Boolean isWriteIndex;

    public IndexAlias() {

    }

    public IndexAlias(String alias, String index, String filter,
                      String routingIndex, String routingSearch, String isWriteIndex) {
        this.alias = alias;
        this.index = index;
        this.filter = filter;
        this.routingIndex = routingIndex;
        this.routingSearch = routingSearch;
        if(StringUtils.isBlank(isWriteIndex)){
            this.isWriteIndex = false;
        }else {
            this.isWriteIndex = Boolean.parseBoolean(isWriteIndex);
        }
    }
}
