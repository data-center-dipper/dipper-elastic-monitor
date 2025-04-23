package com.dipper.monitor.entity.elastic.index;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

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
    private String alians;
    private JSONObject aliansJson;
    private List<String> aliansList;
    private Boolean indexCanWrite;
    private Boolean lifecycleStatus;
    private Integer alinasCanWrite;
}
