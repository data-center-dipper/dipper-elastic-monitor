package com.dipper.monitor.entity.elastic.alians;

import lombok.Data;

@Data
public class AliasPageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
}
