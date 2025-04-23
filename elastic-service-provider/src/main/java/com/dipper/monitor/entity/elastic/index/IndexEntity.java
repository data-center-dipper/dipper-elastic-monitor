package com.dipper.monitor.entity.elastic.index;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class IndexEntity {
    private String health;
    private String status;
    private Boolean freeze;
    private String index;
    private String uuid;
    private Integer pri;
    private Integer rep;
    private Long docsCount;
    private Long docsDeleted;
    private String storeSize;
    private String storeSizeWithUnit;
    private String priStoreSize;
    private String settings;
}
