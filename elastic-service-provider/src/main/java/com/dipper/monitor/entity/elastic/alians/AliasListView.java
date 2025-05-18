package com.dipper.monitor.entity.elastic.alians;

import lombok.Data;

@Data
public class AliasListView {
    private String aliasName;
    private String indexName;
    private Boolean isWriteAble;
}
